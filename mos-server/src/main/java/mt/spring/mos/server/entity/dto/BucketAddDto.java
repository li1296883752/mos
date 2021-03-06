package mt.spring.mos.server.entity.dto;

import lombok.Data;

/**
 * @Author Martin
 * @Date 2020/10/13
 */
@Data
public class BucketAddDto {
	private String bucketName;
	private Boolean defaultIsPublic;
	private Integer dataFragmentsAmount = 1;
}
