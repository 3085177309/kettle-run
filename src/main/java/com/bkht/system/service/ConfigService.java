package com.bkht.system.service;

import com.bkht.core.service.AbstractBaseService;
import com.bkht.system.dao.ConfigDao;
import com.bkht.system.entity.Config;
import com.bkht.system.exception.ConfigCannotDeleteException;
import com.bkht.system.exception.ConfigNameExistException;
import com.bkht.system.utils.ConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 系统配置业务操作类
 *
 * @author sofar
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ConfigService extends AbstractBaseService<Config, Long> {

	private ConfigDao configDao;
	
	@Autowired
	public void setConfigDao(ConfigDao configDao) {
		this.configDao = configDao;
		super.setDao(configDao);
	}
	
	/**
	 * 根据配置代码查找配置对象 并将查询结果加入缓存
	 * @param code 配置代码
	 * @return 配置对象
	 */
	@Cacheable(value="configCache",key="#code")
	public Config findByCode(String code) {
        Assert.notNull(code, "配置代码不能为空");
        List<Config> configList = this.configDao.findByProperty("code", code);
        if (configList.isEmpty()) {
            logger.error("配置代码为[{}]的配置对象不存在，请添加对应配置信息！", code);
            return null;
        }
        if (configList.size() > 1) {
            logger.warn("配置代码为[{}]的配置对象不唯一，请检查数据，当前将使用最后一次添加配置对象信息！");
        }
		return configList.get(0);
	}

	/**
	 * 保存或更新配置对象 并更新缓存对象
	 * @param config 配置对象
	 * @throws Exception 当保存配置对象名称已存在时抛出该异常
	 */
	@CachePut(value="configCache",key="#config.code")
	@Transactional
	public Config saveOrUpdate(Config config) throws Exception {
		Assert.notNull(config, "配置信息不能为null");
		Config c = this.findByCode(config.getCode());
		if (c != null && config.isNew()) {
			logger.error("配置代码{}已存在！",config.getCode());
			throw new ConfigNameExistException("配置代码" + config.getCode() + "已存在！");
		} else {
			if (config.isNew()) { //保存配置对象
				logger.info("添加配置信息，配置信息为：{}", config);
			} else { //更新配置对象
				logger.info("更新配置信息，配置信息为：{}", config);
			}
		}
		return this.save(config);
	}
	
	/**
	 * 删除配置对象
	 * @param id 配置对象Id
	 * @throws ConfigCannotDeleteException
	 */
    @CacheEvict(value="configCache",allEntries=true)
	@Transactional(readOnly=false)
	public Config deleteConfig(Long id) throws Exception {
		Assert.notNull(id, "配置ID不能为空");
		Config config = this.findById(id);
	    return this.deleteConfig(config);
	}

	/**
	 * 批量删除配置信息 并清空缓存
	 * @param ids 配置信息Id数组
	 * @throws ConfigCannotDeleteException
	 */
	@CacheEvict(value="configCache",allEntries=true)
	@Transactional(readOnly=false)
	public void deleteConfigs(Long[] ids) throws Exception {
		for (Long id : ids) {
			this.deleteConfig(id);
		}
	}
	
	/**
	 * 删除配置对象 并将缓存中该对象清空 如果配置类型为系统配置则不允许删除
	 * @param config 配置对象
	 * @return 配置对象
	 * @throws ConfigCannotDeleteException 
	 */
	@CacheEvict(value="configCache",key="#config.code")
	@Transactional(readOnly=false)
	public Config deleteConfig(Config config) throws Exception {
		if (config.getConfigType() == ConfigType.S) {
			logger.error("配置信息{}为系统配置，不允许被删除", config);
			throw new ConfigCannotDeleteException("Config can not be deleted Because it is the System's Config！");
		} else {
			logger.debug("删除配置信息，配置信息为：{}",config);
			this.configDao.delete(config);
		}
		 return config;
	}
	
	@Override
	@Transactional(readOnly = false)
	public void delete(Config t) throws Exception {
		this.deleteConfig(t);
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteById(Long id) throws Exception {
		this.deleteConfig(id);
	}

	@Override
	@Transactional(readOnly = false)
	public void batchDelete(Long[] ids) throws Exception {
		deleteConfigs(ids);
	}
	
	/**
	 * 更新Config对象 并将缓存中该对象清空
	 * @param config Config实例
	 * @throws Exception
	 */
	@CacheEvict(value="configCache",key="#config.code")
	@Transactional(readOnly=false)
	public Config updateConfig(Config config) throws Exception {
		Assert.notNull(config, "配置信息不能为null");
		Long id = config.getId();
		Config cfg = this.findById(id);
		cfg.setValue(config.getValue());
		this.saveOrUpdate(cfg);
		return cfg;
	}

	/**
	 * 判断配置名称是否存在
	 * @param code 配置名称
	 * @return true/false
	 */
	public Boolean existCode(String code) {
		Config config = this.findByCode(code);
		return config != null;
	}

}
