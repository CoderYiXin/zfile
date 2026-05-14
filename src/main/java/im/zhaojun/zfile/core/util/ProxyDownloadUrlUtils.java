package im.zhaojun.zfile.core.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理下载链接工具类
 *
 * @author zhaojun
 */
@Slf4j
public class ProxyDownloadUrlUtils {

	private static SystemConfigService systemConfigService;

	private static final String PROXY_DOWNLOAD_LINK_DELIMITER = ":";

	/**
	 * 永久签名的标记 (写入签名内容第三段以代替过期时间戳).
	 */
	private static final String PERMANENT_SIGNATURE_MARKER = "PERMANENT";

	private static final Map<String, SymmetricCrypto> AES_CACHE = new HashMap<>();

	/**
	 * 服务器代理下载 URL 有效期 (秒).
	 */
	public static final Integer PROXY_DOWNLOAD_LINK_EFFECTIVE_SECOND = 1800;

	/**
	 * 生成签名：根据系统设置中 AES 密钥对生成签名.
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 *
	 * @param 	pathAndName
	 * 			文件路径及文件名称
	 *
	 * @param 	effectiveSecond
	 * 			有效时间, 单位: 秒
	 *
	 * @return	签名
	 */
	public static String generatorSignature(Integer storageId, String pathAndName, Integer effectiveSecond) {
		// 如果有效时间为空, 则设置 30 分钟过期
		if (effectiveSecond == null || effectiveSecond < 1) {
			effectiveSecond = PROXY_DOWNLOAD_LINK_EFFECTIVE_SECOND;
		}

		// 过期时间的秒数
		long second = DateUtil.offsetSecond(DateUtil.date(), effectiveSecond).getTime();
		String content = storageId + PROXY_DOWNLOAD_LINK_DELIMITER + pathAndName + PROXY_DOWNLOAD_LINK_DELIMITER + second;

		return encrypt(content);
	}


	/**
	 * 生成永久签名：用于代理公开下载场景, 签名内容包含路径但不带过期时间.
	 * <p>
	 * 这样即使关闭了"私有空间"开关, 下载链接仍然携带签名,
	 * 服务端通过校验签名内容中的路径来阻止任意路径访问 (路径隔离).
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 *
	 * @param 	pathAndName
	 * 			文件路径及文件名称
	 *
	 * @return	永久签名
	 */
	public static String generatorPermanentSignature(Integer storageId, String pathAndName) {
		String content = storageId + PROXY_DOWNLOAD_LINK_DELIMITER + pathAndName + PROXY_DOWNLOAD_LINK_DELIMITER + PERMANENT_SIGNATURE_MARKER;
		return encrypt(content);
	}


	public static boolean validSignatureExpired(Integer expectedStorageId, String expectedPathAndName, String signature) {
		if (StringUtils.isEmpty(signature)) {
			return false;
		}
		if (systemConfigService == null) {
			systemConfigService = SpringUtil.getBean(SystemConfigService.class);
		}

		String aesHexKey = systemConfigService.getAesHexKeyOrGenerate();
		SymmetricCrypto aes = AES_CACHE.computeIfAbsent(aesHexKey, k -> new SymmetricCrypto(SymmetricAlgorithm.AES, HexUtil.decodeHex(k)));

		long currentTimeMillis = System.currentTimeMillis();

		String storageId = null;
		String pathAndName = null;
		String expiredSecond = null;

		try {
			//解密
			String decryptStr = aes.decryptStr(signature);
			List<String> split = StringUtils.split(decryptStr, PROXY_DOWNLOAD_LINK_DELIMITER);
			storageId = split.get(0);
			pathAndName = split.get(1);
			expiredSecond = split.get(2);

			// 先校验存储源 ID 和文件路径必须匹配, 这是签名最核心的隔离能力.
			boolean storageAndPathMatch = StringUtils.equals(storageId, Convert.toStr(expectedStorageId))
					&& StringUtils.equals(StringUtils.concat(pathAndName), StringUtils.concat(expectedPathAndName));

			if (!storageAndPathMatch) {
				log.warn("校验链接不匹配, signature: {}, storageId={}, pathAndName={}, expiredSecond={}, now:={}", signature, storageId, pathAndName, expiredSecond, currentTimeMillis);
				return false;
			}

			// 永久签名跳过过期时间校验.
			if (PERMANENT_SIGNATURE_MARKER.equals(expiredSecond)) {
				return true;
			}

			// 普通签名继续校验是否过期.
			if (currentTimeMillis < Convert.toLong(expiredSecond)) {
				return true;
			}

			log.warn("校验链接已过期, signature: {}, storageId={}, pathAndName={}, expiredSecond={}, now:={}", signature, storageId, pathAndName, expiredSecond, currentTimeMillis);
		} catch (Exception e) {
			log.error("校验签名链接异常, signature: {}, storageId={}, pathAndName={}, expiredSecond={}, now:={}", signature, storageId, pathAndName, expiredSecond, currentTimeMillis);
			return false;
		}

		return false;
	}


	private static String encrypt(String content) {
		if (systemConfigService == null) {
			systemConfigService = SpringUtil.getBean(SystemConfigService.class);
		}
		String aesHexKey = systemConfigService.getAesHexKeyOrGenerate();
		SymmetricCrypto aes = AES_CACHE.computeIfAbsent(aesHexKey, k -> new SymmetricCrypto(SymmetricAlgorithm.AES, HexUtil.decodeHex(k)));
		return aes.encryptHex(content);
	}

}