package im.zhaojun.zfile.module.storage.model.result;

import im.zhaojun.zfile.module.storage.model.enums.SearchModeEnum;
import im.zhaojun.zfile.module.storage.model.bo.RefreshTokenCacheBO;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 存储源设置后台管理 Result
 *
 * @author zhaojun
 */
@Data
@Schema(description = "存储源设置后台管理 Result")
public class StorageSourceAdminResult {

	@Schema(name = "ID, 新增无需填写", example = "1")
	private Integer id;


	@Schema(name = "是否启用", example = "true")
	private Boolean enable;


	@Schema(name = "是否启用文件操作功能", example = "true", description ="是否启用文件上传，编辑，删除等操作.")
	private Boolean enableFileOperator;


	@Schema(name = "是否允许匿名进行文件操作", example = "true", description ="是否允许匿名进行文件上传，编辑，删除等操作.")
	private Boolean enableFileAnnoOperator;

	@Schema(name = "是否开启缓存", example = "true")
	private Boolean enableCache;


	@Schema(name = "存储源名称", example = "阿里云 OSS 存储")
	private String name;


	@Schema(name = "存储源别名", example = "存储源别名，用于 URL 中展示, 如 http://ip:port/{存储源别名}")
	private String key;


	@Schema(name = "存储源备注", example = "这是一个备注信息, 用于管理员区分不同的存储源, 此字段仅管理员可见")
	private String remark;


	@Schema(name = "是否开启缓存自动刷新", example = "true")
	private Boolean autoRefreshCache;


	@Schema(name = "存储源类型")
	private StorageTypeEnum type;


	@Schema(name = "是否开启搜索", example = "true")
	private Boolean searchEnable;


	@Schema(name = "搜索是否忽略大小写", example = "true")
	private Boolean searchIgnoreCase;


	@Schema(name = "搜索模式", example = "SEARCH_CACHE", description ="仅从缓存中搜索或直接全量搜索")
	private SearchModeEnum searchMode;


	@Schema(name = "排序值", example = "1")
	private Integer orderNum;


	@Schema(name = "是否默认开启图片模式", example = "true")
	private Boolean defaultSwitchToImgMode;


	@Schema(name = "存储源刷新信息")
	private RefreshTokenCacheBO.RefreshTokenInfo refreshTokenInfo;
	
	
	@Schema(name = "兼容 readme 模式", example = "true", description ="兼容模式, 目录文档读取 readme.md 文件")
	private Boolean compatibilityReadme;

}