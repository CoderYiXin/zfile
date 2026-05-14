package im.zhaojun.zfile.module.share.model.request;

import im.zhaojun.zfile.core.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取分享 OnlyOffice 预览配置请求.
 *
 * @author zhaojun
 */
@Data
@Schema(description = "获取分享 OnlyOffice 配置请求")
public class ShareOnlyOfficeConfigTokenRequest {

    @Schema(title = "分享链接 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc12345")
    @NotBlank(message = "分享链接 key 不能为空")
    private String shareKey;

    @Schema(title = "分享内文件路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "/office/文档.docx")
    @NotBlank(message = "文件路径不能为空")
    private String path;

    @Schema(title = "分享密码", example = "123456")
    private String sharePassword;

    @Schema(title = "目录密码", example = "123456")
    private String folderPassword;

    /**
     * 处理默认值, 统一路径规则.
     */
    public void handleDefaultValue() {
        path = StringUtils.concat(path);
    }

}
