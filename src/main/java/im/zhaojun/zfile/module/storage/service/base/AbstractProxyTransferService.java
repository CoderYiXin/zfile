package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.net.url.UrlBuilder;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.ProxyDownloadUrlUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.model.param.ProxyTransferParam;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;

/**
 * 代理传输数据(上传/下载) Service
 *
 * @author zhaojun
 */
public abstract class AbstractProxyTransferService<P extends ProxyTransferParam> extends AbstractBaseFileService<P>{


	/**
	 * 服务器代理下载 URL 前缀.
	 */
	public static final String PROXY_DOWNLOAD_LINK_PREFIX = "/pd";


	/**
	 * 服务器代理下载 URL 前缀.
	 */
	public static final String PROXY_UPLOAD_LINK_PREFIX = "/file/upload";

	@Resource
	private SystemConfigService systemConfigService;


	@Resource
	private StorageSourceService storageSourceService;


	/**
	 * 获取默认代理下载 URL.
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @return  默认的代理下载 URL
	 */
	public String getProxyDownloadUrl(String pathAndName) {
		return getProxyDownloadUrl(pathAndName, false);
	}


	/**
	 * 获取默认代理下载 URL.
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @param 	useParamDomain
	 * 			是否使用存储源参数中的域名替换系统配置中的域名作为下载地址
	 *
	 * @return  默认的代理下载 URL
	 */
	public String getProxyDownloadUrl(String pathAndName, boolean useParamDomain) {
		String path = pathAndName;

		UrlBuilder urlBuilder = UrlBuilder.of();
		String filename = FileUtils.getName(pathAndName);
		if (filename.startsWith(".")) {
			urlBuilder.addQuery("filename", filename);
			path = FileUtils.getParentPath(pathAndName);
		}

		// 无论是否为私有空间, 代理下载链接都必须携带签名,
		// 公开模式使用永久签名, 私有模式使用带过期时间的签名.
		// 这样可以从密码学层面阻止通过修改 URL 中的路径访问其他用户/路径的文件 (#821).
		String signature = param.isProxyPrivate()
				? ProxyDownloadUrlUtils.generatorSignature(storageId, pathAndName, param.getProxyTokenTime())
				: ProxyDownloadUrlUtils.generatorPermanentSignature(storageId, pathAndName);
		urlBuilder.addQuery("signature", signature);

		String url;

		// 如果未填写下载域名，则默认使用带来下载地址.
		if (!useParamDomain || StringUtils.isEmpty(param.getDomain())) {
			String domain = systemConfigService.getAxiosFromDomainOrSetting();
			String storageKey = storageSourceService.findStorageKeyById(storageId);
			url = StringUtils.concat(domain, PROXY_DOWNLOAD_LINK_PREFIX, storageKey, StringUtils.encodeAllIgnoreSlashes(path));
		} else {
			url = StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(path));
		}

		if (StringUtils.isNotEmpty(urlBuilder.getQueryStr())) {
			url = url + "?" + urlBuilder.getQueryStr();
		}
		return url;
	}


	/**
	 * 获取默认代理上传 URL.
	 *
	 * @param   path
	 *          文件路径
	 *
	 * @param   name
	 *          文件名称
	 *
	 * @return  默认的代理下上传 URL
	 */
	public String getProxyUploadUrl(String path, String name) {
		String domain = systemConfigService.getAxiosFromDomainOrSetting();
		String storageKey = storageSourceService.findStorageKeyById(storageId);

		UrlBuilder urlBuilder = UrlBuilder.of();

		String fullPath = StringUtils.concat(path, name);

		// 以 . 开头的文件名, 代表是隐藏文件, 需要特殊处理为参数形式，不然会被安全拦截.
		if (name.startsWith(".")) {
			urlBuilder.addQuery("filename", name);
			fullPath = path;
		}

		String uploadUrl = StringUtils.concat(domain, PROXY_UPLOAD_LINK_PREFIX, storageKey, StringUtils.encodeAllIgnoreSlashes(fullPath));

		if (StringUtils.isNotEmpty(urlBuilder.getQueryStr())) {
			uploadUrl = uploadUrl + "?" + urlBuilder.getQueryStr();
		}

		return uploadUrl;
	}

	/**
	 * 上传文件
	 *
	 * @param   pathAndName
	 *          文件上传路径
	 *
	 * @param   inputStream
	 *          文件流
	 *
	 * @param 	size
	 * 			文件大小
	 */
	public abstract void uploadFile(String pathAndName, InputStream inputStream, Long size) throws Exception;


	/**
	 * 代理下载指定文件。
	 *
	 * 路径契约：pathAndName 必须是相对存储源 basePath 的绝对路径（调用方已拼好用户根目录 /
	 * 分享 sharePath）。实现不得再调用 getCurrentUserBasePath() 二次拼接——代理回源是无登录态
	 * 的新线程，取不到正确的用户根目录。与 getDownloadUrl 同属“绝对入参”族。
	 *
	 * @param   pathAndName
	 *          相对 basePath 的绝对路径（已含用户根目录 / sharePath）
	 *
	 * @return  文件响应.
	 */
	public abstract ResponseEntity<org.springframework.core.io.Resource> downloadToStream(String pathAndName) throws Exception;

    protected SystemConfigService getSystemConfigService() {
        return systemConfigService;
    }

}