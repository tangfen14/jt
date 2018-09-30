package com.jt.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);

    @Autowired(required=false)
    private CloseableHttpClient httpClient;

    @Autowired(required=false)
    private RequestConfig requestConfig;
    
    
    /**
     * 说明:
     * 	 请求方式:Get和POST请求
     * 	 参数如何添加:
     * get请求:		http://www.jd.com/add?id=1&name=2
     *   参数进行封装:
     *   如果用户需要传递参数,则通过指定的方法进行调用即可.
     *   比如准备一个Map<String,String>类型,用户往里面存值,并且要限定用户传参的类型为String;
     *   
     
	  */
    /*方法写法分析:
    	返回值为服务端回传的json,远程对象肯定不能传java对象,因此只能回传json串
    	参数依次为:用户访问的地址,用户上传的参数存入map,字符集编码
    	*/
    public String doGet(String url,Map<String,String> params,String charset){
    	
    	String result = null; //代表访问服务端程序时回传的JSON数据
    	
    	//1.如果用户没有设定具体的字符集,那么我们就指定一个字符集;因此判断字符集编码是否为null,如果为null设定默认字符集
    	if(StringUtils.isEmpty(charset)){
    		
    		charset = "UTF-8";
    	}
    	
    	try {
	    	//判断参数是否为null
	    	if(params != null){
	    		/*	原始写法
	    		 * url  = url + "?";
	    		//www.baidu.com?id=1&name=tom&
	    		for (Map.Entry<String, String> entry: params.entrySet()) {
	    			url  = url + entry.getKey() + "=" + entry.getValue() + "&";
				}
	    		url = url.substring(0, url.length()-1);*/
	    		
	    		//使用httpclient的工具类的写法
	    		URIBuilder builder = new URIBuilder(url);
	    		for (Map.Entry<String,String> entry : params.entrySet()) {
	    			//将所有的参数都封装到builder工具对象中
	    			builder.addParameter(entry.getKey(),entry.getValue());
				}
	    		//下行方法会自动的拼接?和&符;结果为:  http://www.baidu.com?id=1&name=tom
	    		url = builder.build().toString();
	    		
	    	}
	    	
	    	//System.out.println("访问的请求:" + url);
	    	//定义请求的类型
	    	HttpGet httpGet = new HttpGet(url);
	    	//通过动态注入读取配置文件
	    	httpGet.setConfig(requestConfig);
	    	
	    	//通过httpClient发送请求,获得返回的响应对象
	    	CloseableHttpResponse httpResponse = 
	    			httpClient.execute(httpGet);
	    	
	    	//判断是否正确
	    	if(httpResponse.getStatusLine().getStatusCode() == 200){
	    		//获取返回值数据,将响应对象转换为json,并指定字符集编码格式
	    		result = 
	    		EntityUtils.toString(httpResponse.getEntity(),charset);
	    	}
	    	
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    //为了用户方便,多重载几个方法
    public String doGet(String url,Map<String,String> params){
    	//直接调上面的方法
    	return doGet(url, params, null);
    }
    
    public String doGet(String url){
    	
    	return doGet(url, null, null) ;
    }
    
    //实现httpClient中的post请求
    public String doPost(String url,Map<String,String> params,String charset){
    	
    	String result = null;
    	//判断字符集编码
    	if(StringUtils.isEmpty(charset)){
    		//否则肯定乱码
    		charset = "UTF-8";
    	}
    	
    	/**
    	 * 2.与get的方式不同的是,post提交需要先创建请求对象;原因:一般进行post提交的时候,我们要拿到
    	 * 这个请求的对象以后,再通过post的方式携带我们的参数,去访问后台的数据;
    	 * 步骤:
    	 * 	 2.1创建表单实体对象封装参数.
    	 *   2.2将表单对象保存到Post对象中
    	 *   2.3之后发起请求 	
    	 */
    	//创建请求对象类型,并读取配置文件
    	HttpPost httpPost = new HttpPost(url);
    	httpPost.setConfig(requestConfig);
    	
    	
    	try {
	    	//判断是否有参数,有参数的话就封装,没有的话就直接访问
	    	if(params != null){
	    		//定义表单实体对象中需要的List集合,存入form表单中需要传递的数据参数NameValuePair;
	    		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
	    		
	    		//获取用户传递的数据
	    		for (Map.Entry<String,String> entry: params.entrySet()) {
	    			//NameValuePair是一个接口,BasicNameValuePair是它的实现类(ctrl+p查找实现类),其中封装了一个name一个value;
	    			BasicNameValuePair pair = 
	    		new BasicNameValuePair(entry.getKey(), entry.getValue());
	    			//将pair对象赋值给list集合
	    			parameters.add(pair);
				}
	    		//2.1 创建表单实体对象,构造方法中需要一个字符集格式,另一个是List集合,这个List中保存的就是form表单中需要传递的数据;
	    		UrlEncodedFormEntity formEntity = 
	    				new UrlEncodedFormEntity(parameters,charset);
	    		
	    		//2.2将请求实体添加到请求对象中
	    		httpPost.setEntity(formEntity);
	    	}
	    	
	    	//2.3实现post请求(正式发起请求,获取响应结果)
	    	CloseableHttpResponse httpResponse = 
	    	httpClient.execute(httpPost);
	    		//判断结果,并解析返回值信息
	    	if(httpResponse.getStatusLine().getStatusCode() == 200){
	    		result = EntityUtils.toString(httpResponse.getEntity(),charset);
	    	}
	    	
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return result;
    }
    //方便用户,多重载几个方法
    public String doPost(String url,Map<String,String> params){
    	
    	return doPost(url, params, null);
    }
    
    public String doPost(String url){
    	
    	return doPost(url, null, null);
    }
}
