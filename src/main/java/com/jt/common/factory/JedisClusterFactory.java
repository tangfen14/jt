package com.jt.common.factory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

//通过工厂模式创建JedisCluster对象,业务层使用jedisCluster对象就可以操作redis集群了
public class JedisClusterFactory implements FactoryBean<JedisCluster>{
	
	private Resource propertySource; //表示注入properties文件
	private JedisPoolConfig poolConfig; //注入池对象
	private String redisNodePrefix;		//定义redis节点的前缀
	
	//spring原生工厂模式自动调用此方法,得到jedisCluster对象交给spring管理
	@Override
	public JedisCluster getObject() throws Exception {
		//定义方法,通过源文件获取节点信息
		Set<HostAndPort> nodes = getNodes();  
		JedisCluster jedisCluster = 
				new JedisCluster(nodes, poolConfig);
		
		return jedisCluster;
	}
	
	//获取redis节点Set集合
	public Set<HostAndPort> getNodes(){
		
		//1.准备Set集合
		Set<HostAndPort> nodes = new HashSet<HostAndPort>();
		
		//2.创建property对象
		Properties properties = new Properties();
		try {
			//由property对象通过输入流的方式来读取源文件
			properties.load(propertySource.getInputStream());
			//3.从配置文件中遍历redis节点数据
				//property中也是k-v键值对,它的整体结构不重要,因此我们直接用object来装
				//properties.keySet()返回的是一个set集合
			for (Object key : properties.keySet()) {
				String keyStr = (String) key;
				//获取redis节点数据,判断指定前缀的才进行取值
				if(keyStr.startsWith(redisNodePrefix)){
					//IP:端口
					String value = properties.getProperty(keyStr);
					//用:把值截断分为ip和端口,得到两个字符串放在同一个数组中
					String[] args = value.split(":");
					//将端口转为int类型
					HostAndPort hostAndPort = 
				new HostAndPort(args[0],Integer.parseInt(args[1]));
					nodes.add(hostAndPort);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodes;
	}
	
	
	
	
	

	@Override
	public Class<?> getObjectType() {
		
		return JedisCluster.class;
	}

	@Override
	public boolean isSingleton() {
		
		return false;
	}

	public Resource getPropertySource() {
		return propertySource;
	}

	public void setPropertySource(Resource propertySource) {
		this.propertySource = propertySource;
	}

	public JedisPoolConfig getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(JedisPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	public String getRedisNodePrefix() {
		return redisNodePrefix;
	}

	public void setRedisNodePrefix(String redisNodePrefix) {
		this.redisNodePrefix = redisNodePrefix;
	}
}
