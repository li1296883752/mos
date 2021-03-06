package mt.spring.mos.server.entity.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mt.common.annotation.ForeignKey;
import mt.spring.mos.server.entity.BaseEntity;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @Author Martin
 * @Date 2020/5/20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "mos_access_control")
public class AccessControl extends BaseEntity {
	private static final long serialVersionUID = -1377191126823875077L;
	
	@Id
	@KeySql(useGeneratedKeys = true)
	private Long openId;
	private String secretKey;
	@ForeignKey(tableEntity = Bucket.class, casecadeType = ForeignKey.CascadeType.ALL)
	private Long bucketId;
	private String useInfo;
	@ForeignKey(tableEntity = User.class, casecadeType = ForeignKey.CascadeType.ALL)
	private Long userId;
	@Transient
	private String bucketName;
}
