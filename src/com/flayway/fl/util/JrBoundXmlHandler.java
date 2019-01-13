package com.flayway.fl.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.flayway.fl.bound.FlJrBound;
import com.flayway.fl.data.IHeaderDataValueGetter;
import com.flayway.fl.packer.MessagePackerItem;

public class JrBoundXmlHandler {
	
	private Log log = LogFactory.getLog(FlJrBound.class);
	
	private Document doc;
	public JrBoundXmlHandler(Document doc) {
		this.doc = doc;
	}
	public JrBoundXmlHandler() {
		InputStream is = JrBoundXmlHandler.class.getClassLoader().getResourceAsStream("jr-bound.xml");
		//File xml = new File(DomXmlHandler.class.getClassLoader().getResource("jr.xml").getFile());
		SAXReader saxReader = new SAXReader();
		try {
			doc = saxReader.read(is);
			log.info("创建文档成功");
		} catch (DocumentException e) {
			throw new RuntimeException("读取默认接入通道配置文件失败", e);
		}
	}
	public Document getDoc() {
		return doc;
	}
	public void setDoc(Document doc) {
		this.doc = doc;
	}
	
	//获取请求码解析器类
	public String getReqCode(String boundId) {
		String req = null;
		Element root = doc.getRootElement();
		Iterator<?> iters = root.elementIterator();
		while(iters.hasNext()) {
			Element element = (Element) iters.next();
			if(element.attribute("id").getText().equals(boundId)) {
				Element reqcode = element.element("reqcode");
				req = reqcode.attribute("class").getText();
				log.info("获取请求码解析器类: " + req);
				break;
			}
		}
		return req;
	}
	
	//请求获取方式
	public Map<String, Object> getReqCodeCode(String boundId) {
		HashMap<String, Object> map = new HashMap<String,Object>();
		Element root = doc.getRootElement();
		Iterator<?> iters = root.elementIterator();
		while(iters.hasNext()) {
			Element element = (Element) iters.next();
			if(element.attribute("id").getText().equals(boundId)) {
				Element reqcode = element.element("reqcode");
				String type = reqcode.attribute("type").getText();
				
				if("json".equals(type)) {
					String req = reqcode.element("param").attribute("value").getText();
					map.put(IHeaderDataValueGetter.TRANSCODE, req);
					log.info("json类型请求码字段获取: " + req);
				}
				if("tem".equals(type)) { //定长
					String start = reqcode.element("start").attribute("value").getText();
					String end = reqcode.element("end").attribute("value").getText();
					map.put(IHeaderDataValueGetter.START, start);
					map.put(IHeaderDataValueGetter.END, end);
					log.info("定长报文请求码: " + start + " --to-- " + end + "(byte)");
				}
				break;
			}
		}
		return map;
	}
	
	
	//获取请求码解析器类 处理的报文类型
	public String getReqCodeType(String boundId) {
		String req = null;
		Element root = doc.getRootElement();
		Iterator<?> iters = root.elementIterator();
		while(iters.hasNext()) {
			Element element = (Element) iters.next();
			if(element.attribute("id").getText().equals(boundId)) {
				Element reqcode = element.element("reqcode");
				req = reqcode.attribute("type").getText();
				log.info("获取请求码解析器类报文类型: " + req);
				break;
			}
		}
		return req;
	}
	
	
	//获取报文处理器类（打包解包类）
	public String getMessageResolver(String boundId) {
		String req = null;
		Element root = doc.getRootElement();
		Iterator<?> iters = root.elementIterator();
		while(iters.hasNext()) {
			Element element = (Element) iters.next();
			if(element.attribute("id").getText().equals(boundId)) {
				Element reqcode = element.element("messageresolver");
				Attribute attr = reqcode.attribute("class");
				req = attr.getText();
				log.info("获取报文处理器类: " + req);
				break;
			}
		}
		return req;
	}
	//获取报文处理器类的 VO 
	public String getMessageResolverVo(String boundId, String type) {
		if(type == null) {
			throw new RuntimeException("type 不能为空 (req,resp,exp)");
		}
		String req = null;
		Element root = doc.getRootElement();
		Iterator<?> iters = root.elementIterator();
		while(iters.hasNext()) {
			Element element = (Element) iters.next();
			if(element.attribute("id").getText().equals(boundId)) {
				Element reqcode = element.element("messageresolver");
				Iterator<?> iterss = reqcode.elementIterator("vo");
				while(iterss.hasNext()) {
					Element el = (Element) iterss.next();
					if( type.equals(el.attribute("type").getText()) ) {
						req = el.attribute("class").getText();
						break;
					}
				}
				log.info("获取报文处理器 VO: " + req);
				break;
			}
		}
		return req;
	}
	
	//定长报文Vo信息获取
	public List<MessagePackerItem> getTemMessagePackerVoInfo(String boundId, String type) {
		List<MessagePackerItem> lst = new ArrayList<MessagePackerItem>();
		
		if(type == null) {
			throw new RuntimeException("type 不能为空 (req,resp,exp)");
		}
		Element root = doc.getRootElement();
		Iterator<?> iters = root.elementIterator();
		while(iters.hasNext()) {
			Element element = (Element) iters.next();
			if(element.attribute("id").getText().equals(boundId)) {
				Element reqcode = element.element("messageresolver");
				Iterator<?> iterss = reqcode.elementIterator("vo");
				while(iterss.hasNext()) {
					Element el = (Element) iterss.next();
					if( type.equals(el.attribute("type").getText()) ) {
						Iterator<?> pl = el.elementIterator();
						while(pl.hasNext()) {
							Element p = (Element) pl.next();
							MessagePackerItem item = new MessagePackerItem();
							item.setName(p.attribute("name").getText());
							item.setLength(p.attribute("length").getText());
							Map<Object, Object> map = new HashMap<Object, Object>();
							
							log.info("定长报文类型vo信息获取 : " + item.getName() + " : " + item.getLength());
							
							if("param".equals(p.getName())) {
								item.setType("param");
							} else if("list".equals(p.getName())) {
								item.setType("list");
								try {
									item.setClz(Class.forName(p.attribute("class").getText()));
								} catch (ClassNotFoundException e) {
									throw new RuntimeException("无法获取list中pojo的类型");
								}
								Iterator<?> its = p.element("pojo").elementIterator();
								while(its.hasNext()) {
									Element l = (Element) its.next();
									map.put(l.attribute("name").getText(), l.attribute("length").getText());
								}
								item.setMap(map);
								log.info("定长报文list类型vo信息获取 : " + map);
							} else if("pojo".equals(p.getName())) {
								item.setType("pojo");
								try {
									item.setClz(Class.forName(p.attribute("class").getText()));
								} catch (ClassNotFoundException e) {
									throw new RuntimeException("无法获取pojo中pojo的类型");
								}
								Iterator<?> its = p.elementIterator();
								while(its.hasNext()) {
									Element l = (Element) its.next();
									map.put(l.attribute("name").getText(), l.attribute("length").getText());
								}
								item.setMap(map);
								log.info("定长报文pojo类型vo信息获取 : " + map);
							} else if("map".equals(p.getName())) {
								item.setType("map");
								Iterator<?> its = p.elementIterator();
								while(its.hasNext()) {
									Element l = (Element) its.next();
									map.put(l.attribute("name").getText(), l.attribute("length").getText());
								}
								item.setMap(map);
								log.info("定长报文map类型vo信息获取 : " + map);
							} else {
								throw new RuntimeException("定长报文VO的格式信息解析出错，不支持的标签格式");
							}
							lst.add(item);
						}
					}
				}
			}
		}
		return lst;
	}
	
	//请求码，处理器映射
	public Map<Object,Object> getReqMap(String boundId) {
		Map<Object,Object> reqMap = new HashMap<Object,Object>();
		Element root = doc.getRootElement();
		Iterator<?> iters = root.elementIterator();
		while(iters.hasNext()) {
			Element element = (Element) iters.next();
			if(element.attribute("id").getText().equals(boundId)) {
				Iterator<?> reqs = element.elementIterator("reqmap");
				while(reqs.hasNext()) {
					Element req = (Element) reqs.next();
					String key = req.element("code").getTextTrim();
					String value = req.element("handler").getTextTrim();
					reqMap.put(key, value);
				}
				log.info("获取请求码、处理器映射: " + reqMap);
				break;
			}
		}
		return reqMap;
	}
	
	public static void main(String argv[]) throws Exception{
		JrBoundXmlHandler dom = new JrBoundXmlHandler();
		dom.getTemMessagePackerVoInfo("temBound", "req");
		
		ByteArrayBuilder bab = new ByteArrayBuilder();
		
		bab.write("123".getBytes());
		bab.write("456".getBytes());
		
		byte[] r = bab.toByteArray();
		
		byte[] b = new byte[1];
		
		System.out.println(new String(b));
		
	}
	
}
