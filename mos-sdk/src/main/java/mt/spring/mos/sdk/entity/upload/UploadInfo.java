package mt.spring.mos.sdk.entity.upload;

import lombok.Data;

/**
 * @Author Martin
 * @Date 2020/11/25
 */
@Data
public class UploadInfo {
	private String pathname;
	private boolean cover;
	private boolean isPublic;
	private String contentType;
	
	public UploadInfo(String pathname, boolean cover) {
		this.pathname = pathname;
		this.cover = cover;
	}
	
	public UploadInfo(String pathname, boolean cover, boolean isPublic, String contentType) {
		this.pathname = pathname;
		this.cover = cover;
		this.isPublic = isPublic;
		this.contentType = contentType;
	}
}
