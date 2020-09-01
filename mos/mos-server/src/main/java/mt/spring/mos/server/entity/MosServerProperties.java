package mt.spring.mos.server.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @Author Martin
 * @Date 2020/5/19
 */
@ConfigurationProperties(prefix = "mos.server")
@Data
@Component
public class MosServerProperties {
	/**
	 * 数据分片数量
	 */
	private Integer dataFragmentsAmount = 2;
	/**
	 * 备份超时时间
	 */
	private Integer backReadTimeout = 30 * 60 * 1000;
	/**
	 * 空闲空间GB，如果剩余空间少于这个数，则不允许上传
	 */
	private BigDecimal minAvaliableSpaceGB = BigDecimal.valueOf(4);
	/**
	 * 是否需要验证签名
	 */
	private Boolean isCheckSign = true;
	
	private String defaultBucketName = "default";
	
	private String adminUsername = "admin";
	private String adminPassword = "admin";
	private String registPwd;
	
}