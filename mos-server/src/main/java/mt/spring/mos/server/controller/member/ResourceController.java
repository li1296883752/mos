package mt.spring.mos.server.controller.member;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import mt.common.annotation.CurrentUser;
import mt.common.entity.ResResult;
import mt.spring.mos.server.annotation.NeedPerm;
import mt.spring.mos.server.entity.BucketPerm;
import mt.spring.mos.server.entity.dto.CheckFileExistsDto;
import mt.spring.mos.server.entity.dto.ResourceCopyDto;
import mt.spring.mos.server.entity.dto.ResourceSearchDto;
import mt.spring.mos.server.entity.dto.ResourceUpdateDto;
import mt.spring.mos.server.entity.po.*;
import mt.spring.mos.server.entity.vo.CheckFileExistsBo;
import mt.spring.mos.server.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Martin
 * @Date 2020/5/29
 */
@RestController
@RequestMapping("/member/resource")
public class ResourceController {
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private BucketService bucketService;
	@Autowired
	private DirService dirService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private BucketGrantService bucketGrantService;
	
	@DeleteMapping("/{bucketName}/del")
	@NeedPerm(BucketPerm.DELETE)
	public ResResult del(@PathVariable String bucketName, Long[] dirIds, Long[] fileIds, @CurrentUser User currentUser) {
		Assert.state(dirIds != null || fileIds != null, "要删除的文件或文件夹不能为空");
		Bucket bucket = bucketService.findBucketByUserIdAndBucketName(currentUser.getId(), bucketName);
		Assert.notNull(bucket, "bucket不能为空");
		resourceService.deleteResources(bucket, dirIds, fileIds);
		return ResResult.success();
	}
	
	@PutMapping("/{bucketName}/{id}")
	@NeedPerm(BucketPerm.UPDATE)
	public ResResult update(@PathVariable String bucketName, @PathVariable Long id, @RequestBody ResourceUpdateDto resourceUpdateDto, @CurrentUser User currentUser) {
		resourceUpdateDto.setId(id);
		resourceService.updateResource(resourceUpdateDto, currentUser.getId(), bucketName);
		return ResResult.success();
	}
	
	@GetMapping("/{bucketName}/list")
	@NeedPerm(BucketPerm.SELECT)
	public ResResult list(
			ResourceSearchDto resourceSearchDto,
			@PathVariable String bucketName,
			@ApiIgnore Bucket bucket
	) {
		auditService.doAudit(bucket.getId(), resourceSearchDto.getPath() == null ? "/" : resourceSearchDto.getPath(), Audit.Type.READ, Audit.Action.list, null, 0);
		JSONObject data = new JSONObject();
		data.put("bucketName", bucketName);
		data.put("resources", resourceService.findDirAndResourceVoListPage(resourceSearchDto, bucket.getId()));
		List<Dir> parentDirs = Collections.emptyList();
		Dir currentDir = null;
		Dir lastDir = null;
		if (StringUtils.isNotBlank(resourceSearchDto.getPath())) {
			//当前路径搜索
			currentDir = dirService.findOneByPathAndBucketId(resourceSearchDto.getPath(), bucket.getId());
			if (currentDir != null) {
				parentDirs = dirService.findAllParentDir(currentDir);
				Collections.reverse(parentDirs);
			}
			if (CollectionUtils.isNotEmpty(parentDirs)) {
				lastDir = parentDirs.get(parentDirs.size() - 1);
			}
		} else {
			currentDir = new Dir();
			currentDir.setPath("");
		}
		data.put("currentDir", currentDir);
		data.put("lastDir", lastDir);
		data.put("parentDirs", parentDirs);
		return ResResult.success(data);
	}
	
	@PostMapping("/copy/{bucketName}/to/{desBucketName}")
	@NeedPerm(BucketPerm.SELECT)
	@ApiOperation("复制资源")
	public ResResult copy(@ApiIgnore @CurrentUser User currentUser, @PathVariable String bucketName, @PathVariable String desBucketName, @RequestBody ResourceCopyDto resourceCopyDto) {
		Bucket srcBucket = bucketService.findBucketByUserIdAndBucketName(currentUser.getId(), bucketName);
		Bucket desBucket = bucketService.findBucketByUserIdAndBucketName(currentUser.getId(), desBucketName);
		Assert.state(bucketGrantService.hasPerms(currentUser.getId(), desBucket, BucketPerm.INSERT), desBucketName + "没有权限");
		resourceService.copyToBucket(resourceCopyDto, srcBucket, desBucket);
		return ResResult.success();
	}
	
	@NeedPerm(perms = BucketPerm.SELECT)
	@PostMapping("/{bucketName}/checkFile/isExists")
	@ApiOperation("批量判断文件是否存在")
	public ResResult isBatchExists(@RequestBody CheckFileExistsDto checkFileExistsDto, @ApiIgnore Bucket bucket, @PathVariable String bucketName, @ApiIgnore @CurrentUser User currentUser) {
		Assert.notNull(checkFileExistsDto, "检查文件不能为空");
		Map<String, Boolean> checkResult = new HashMap<>();
		for (String pathname : checkFileExistsDto.getPathnames()) {
			Resource resource = resourceService.findResourceByPathnameAndBucketId(pathname, bucket.getId());
			checkResult.put(pathname, resource != null);
		}
		CheckFileExistsBo checkFileExistsBo = new CheckFileExistsBo();
		checkFileExistsBo.setCheckResults(checkResult);
		return ResResult.success(checkFileExistsBo);
	}
}
