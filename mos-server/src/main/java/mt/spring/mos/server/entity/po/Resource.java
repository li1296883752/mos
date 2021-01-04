package mt.spring.mos.server.entity.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mt.common.annotation.ForeignKey;
import mt.spring.mos.base.utils.SizeUtils;
import mt.spring.mos.server.entity.BaseEntity;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.File;

/**
 * @Author Martin
 * @Date 2020/5/18
 */
@Table(name = "mos_resource")
@Data
@EqualsAndHashCode(callSuper = false)
public class Resource extends BaseEntity {
	
	private static final long serialVersionUID = 721502363752246263L;
	
	@Id
	@KeySql(useGeneratedKeys = true)
	private Long id;
	@Column(nullable = false)
	private String name;
	private String contentType;
	@Column(nullable = false)
	private Long sizeByte;
	@ForeignKey(tableEntity = Dir.class, casecadeType = ForeignKey.CascadeType.ALL)
	@Column(nullable = false)
	private Long dirId;
	private Boolean isPublic;
	@ForeignKey(tableEntity = FileHouse.class)
	private Long fileHouseId;
	@ForeignKey(tableEntity = FileHouse.class)
	private Long thumbFileHouseId;
	private String suffix;
	private Integer thumbFails;
	private Long visits;
	private Long lastModified;
	
	public Integer getThumbFails() {
		return thumbFails == null ? 0 : thumbFails;
	}
	
	public String getContentType() {
		if (contentType != null) {
			return contentType;
		}
		return null;
	}
	
	@Transient
	public String getReadableSize() {
		return SizeUtils.getReadableSize(sizeByte);
	}
	
	@Transient
	public String getExtension() {
		String fileName = getName();
		if (fileName == null) {
			return null;
		}
		int lastIndexOf = fileName.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return null;
		}
		return fileName.substring(lastIndexOf + 1);
	}
	
	public Boolean getIsPublic() {
		return this.isPublic == null ? false : this.isPublic;
	}
}
