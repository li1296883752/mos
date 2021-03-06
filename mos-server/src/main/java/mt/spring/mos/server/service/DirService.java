package mt.spring.mos.server.service;

import mt.common.mybatis.mapper.BaseMapper;
import mt.common.service.BaseServiceImpl;
import mt.common.tkmapper.Filter;
import mt.spring.mos.server.dao.DirMapper;
import mt.spring.mos.server.entity.dto.DirUpdateDto;
import mt.spring.mos.server.entity.po.Audit;
import mt.spring.mos.server.entity.po.Dir;
import mt.utils.common.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Martin
 * @Date 2020/5/20
 */
@Service
public class DirService extends BaseServiceImpl<Dir> {
	@Autowired
	private DirMapper dirMapper;
	@Autowired
	private BucketService bucketService;
	@Autowired
	private LockService lockService;
	@Autowired
	private AuditService auditService;
	@Autowired
	@Lazy
	private ResourceService resourceService;
	
	@Override
	public BaseMapper<Dir> getBaseMapper() {
		return dirMapper;
	}
	
	public List<Dir> findAllParentDir(Dir dir) {
		List<Dir> dirs = new ArrayList<>();
		Long parentId = dir.getParentId();
		while (parentId != null) {
			Dir parent = findById(parentId);
			dirs.add(parent);
			parentId = parent.getParentId();
		}
		return dirs;
	}
	
	public Dir findOneByPathAndBucketId(String path, Long bucketId) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		List<Filter> filters = new ArrayList<>();
		filters.add(new Filter("path", Filter.Operator.eq, path));
		filters.add(new Filter("bucketId", Filter.Operator.eq, bucketId));
		return findOneByFilters(filters);
	}
	
	public Dir findOneByDirIdAndBucketId(Long dirId, Long bucketId) {
		List<Filter> filters = new ArrayList<>();
		filters.add(new Filter("id", Filter.Operator.eq, dirId));
		filters.add(new Filter("bucketId", Filter.Operator.eq, bucketId));
		return findOneByFilters(filters);
	}
	
	public String getParentPath(String pathname) {
		if (!pathname.startsWith("/")) {
			pathname = "/" + pathname;
		}
		int lastIndexOf = pathname.lastIndexOf("/");
		String parentPath = pathname.substring(0, lastIndexOf);
		if (StringUtils.isBlank(parentPath)) {
			return "/";
		}
		return parentPath;
	}
	
	@Transactional
	public Dir addDir(String path, Long bucketId) {
		if (!"/".equals(path) && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String finalPath = path;
		
		return lockService.doWithLock("addDir-" + path + "-" + bucketId, LockService.LockType.WRITE, 5, () -> {
			Dir findDir = findOneByPathAndBucketId(finalPath, bucketId);
			if (findDir != null) {
				return findDir;
			}
			Dir parentDir = null;
			if (!"/".equalsIgnoreCase(finalPath)) {
				String parentPath = getParentPath(finalPath);
				parentDir = addDir(parentPath, bucketId);
			}
			
			Dir dir = new Dir();
			dir.setPath(finalPath);
			dir.setBucketId(bucketId);
			if (parentDir != null) {
				dir.setParentId(parentDir.getId());
			}
			auditService.doAudit(bucketId, finalPath, Audit.Type.WRITE, Audit.Action.addDir);
			save(dir);
			return dir;
		});
	}
	
	@Override
	public Dir findOneByFilters(List<Filter> filters) {
		return super.findOneByFilters(filters);
	}
	
	@Transactional
	public void updatePath(Long bucketId, DirUpdateDto dirUpdateDto) {
		String newPath = dirUpdateDto.getPath();
		Assert.notBlank(newPath, "路径不能为空");
		Assert.state(!newPath.contains(".."), "非法路径：" + newPath);
		if (!newPath.startsWith("/")) {
			newPath = "/" + newPath;
		}
		Dir findDir = findOneByPathAndBucketId(newPath, bucketId);
		Assert.state(findDir == null, "路径" + newPath + "已存在");
		Dir parentDir = addDir(getParentPath(newPath), bucketId);
		Dir currentDir = findById(dirUpdateDto.getId());
		auditService.doAudit(currentDir.getBucketId(), currentDir.getPath(), Audit.Type.WRITE, Audit.Action.updateDir, currentDir.getPath() + "->" + newPath, 0);
		currentDir.setParentId(parentDir.getId());
		currentDir.setPath(newPath);
		updateById(currentDir);
	}
	
	private void updateParentDir(Dir dir, Dir parentDir) {
		String path = dir.getPath();
		File file = new File(path);
		String name = file.getName();
		String desPath = parentDir.getPath() + "/" + name;
		dir.setParentId(parentDir.getId());
		dir.setPath(desPath);
		updateById(dir);
		List<Dir> children = findList("parentId", dir.getId());
		if (CollectionUtils.isNotEmpty(children)) {
			for (Dir child : children) {
				updateParentDir(child, dir);
			}
		}
	}
	
	@Transactional
	public void mergeDir(Long bucketId, Long srcId, Long desId) {
		Dir srcDir = findOneByDirIdAndBucketId(srcId, bucketId);
		Dir desDir = findOneByDirIdAndBucketId(desId, bucketId);
		Assert.notNull(srcDir, "源路径不存在");
		Assert.notNull(desDir, "目标路径不存在");
		//把srcDir下的子目录移过去
		List<Dir> children = findList("parentId", srcDir.getId());
		if (CollectionUtils.isNotEmpty(children)) {
			for (Dir child : children) {
				updateParentDir(child, desDir);
			}
		}
		//把srcDir下的文件移过去
		resourceService.changeDir(srcId, desId);
		//删除原文件夹
		deleteById(srcDir);
	}
}
