package com.flayway.fl.bound;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.flayway.fl.data.HeaderDataValueInfo;
import com.flayway.fl.data.IHeaderDataValueGetter;
import com.flayway.fl.packer.IMessagePacker;
import com.flayway.fl.packer.MessagePackerInfo;
import com.flayway.fl.resolver.IReqResolver;
import com.flayway.fl.util.JrBoundXmlHandler;

public class FlJrBound {
	
	private String id;                     //标识每一个类
	private static Map<String,Object> idHandler = new HashMap<String,Object>();  //记录id
	private String host;
	private int port;
	private int pool;
	private ServerSocketChannel schan;
	private Selector selector;
	private Dispatcher dispatcher;
	private String defaultCharset = "UTF-8";
	
	private Log log = LogFactory.getLog(FlJrBound.class);
	
	//保存交易码和处理类的对应关系
	private Map<Object,Object> handlerMap = new HashMap<Object,Object>();
	//交易码获取器
	private IHeaderDataValueGetter reqCodeValueGetter;
	
	//报文打包解包类
	private IMessagePacker messagePacker;
	//请求报文处理器类Vo
	private Class<?> reqMessagePackerVo;
	//响应报文处理器类Vo
	private Class<?> respMessagePackerVo;
	//异常报文处理器类Vo
	private Class<?> expMessagePackerVo;
	
	
	public FlJrBound(String id, String host, int port, int pool) {
		if(idExists(id)) {
			throw new RuntimeException("id已存在，创建通道类失败");
		}
		this.id = id;
		handlerMap.put(id, this.getClass());
		log.info("记录通道id: " + id);
		this.host = host;
		this.port = port;
		this.pool = pool;
		dispatcher = new Dispatcher();
		init();
		initialize();
		startChannel();
	}
	
	private void startChannel() {
		while(true){
			try{
				if(selector.select() == 0) {
					continue;
				}
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iters = selectedKeys.iterator();
				while(iters.hasNext()) {
					SelectionKey key = iters.next();
					iters.remove();
					if(key.isAcceptable()) {
						ServerSocketChannel channel = (ServerSocketChannel) key.channel();
						SocketChannel client = channel.accept();
						log.info("获取一个连接: " + client);
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
					}
					if(key.isReadable()) {
						dispatcher.handler(new ServerThread(key));
						key.cancel();
					}
				}
			}catch(IOException e) {
				throw new RuntimeException("开启通道失败",e);
			}
		}
	}
	
	private boolean idExists(String id) {
		if(idHandler.containsKey(id)) {
			return true;
		}
		return false;
	}
	
	public void init() {
		try {
			schan = ServerSocketChannel.open();
			schan.configureBlocking(false);
			ServerSocket socket = schan.socket();
			socket.bind(new InetSocketAddress(host, port), pool);
			selector = Selector.open();
			schan.register(selector, SelectionKey.OP_ACCEPT);
			log.info("通道: " + schan + "注册 OP_ACCEPT事件");
		} catch (IOException e) {
			throw new RuntimeException("通道创建异常," + e.getMessage(), e);
		}
	}
	
	
	public void initialize() {
		
		JrBoundXmlHandler dom = new JrBoundXmlHandler();
		try {
			
			this.reqCodeValueGetter = (IHeaderDataValueGetter) Class.forName(dom.getReqCode(this.id)).newInstance();
			log.info("获取通道: " + id + " 的请求码解析器: " + dom.getReqCode(this.id));
			
			this.messagePacker = (IMessagePacker) Class.forName(dom.getMessageResolver(this.id)).newInstance();
			this.reqMessagePackerVo = Class.forName(dom.getMessageResolverVo(this.id, "req"));
			this.respMessagePackerVo = Class.forName(dom.getMessageResolverVo(this.id, "resp"));
			this.expMessagePackerVo = Class.forName(dom.getMessageResolverVo(this.id, "exp"));
			
			Map<Object, Object> map = dom.getReqMap(this.id);
			Iterator<Entry<Object, Object>> iters = map.entrySet().iterator();
			while(iters.hasNext()) {
				Entry<Object, Object> entry = iters.next();
				if(handlerMap.containsKey(entry.getKey())) {
					throw new RuntimeException("交易码设置失败, " + "通道: " + id + " 的交易码已存在");
				}
				handlerMap.put(entry.getKey(), entry.getValue());
			}
			log.info("获取通道: " + id + " 的交易码、处理器映射: " + handlerMap);
			
		} catch (InstantiationException e) {
			throw new RuntimeException("创建处理器类失败", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("创建处理器类失败", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("处理器类路径无法找到", e);
		}
		
	}
	
	class Dispatcher {
		public void handler(Runnable run) {
			if(run != null) {
				new Thread(run).start();
			}
		}
	}
	
	class ServerThread implements Runnable {
		
		private SelectionKey key;
		public ServerThread(SelectionKey key) {
			this.key = key;
		}
		
		@Override
		public void run() {
			SocketChannel client = null;
			
			try {
				client = (SocketChannel) key.channel();
				ByteBuffer bb = ByteBuffer.allocate(4);
				bb.clear();
				client.read(bb);
				int recvLen = Integer.parseInt(new String(bb.array(),defaultCharset));
				log.info("数据包长度信息: " + recvLen);
				bb.clear();
				bb = ByteBuffer.allocate(recvLen);
				int readLen = client.read(bb);
				log.info("读取的数据包长度: " + readLen);
				byte[] readBy = bb.array();
				log.info("获取的数据包: " + readBy);
				
				//生成请求码获取器的info信息
				JrBoundXmlHandler dom = new JrBoundXmlHandler();
				Map<String, Object> reqMap = dom.getReqCodeCode(id);
				HeaderDataValueInfo info = new HeaderDataValueInfo();
				info.cpyMap(reqMap);
				info.put(IHeaderDataValueGetter.REQCODEVO, reqMessagePackerVo);
				info.put(IHeaderDataValueGetter.ENCODING, defaultCharset);
				//调用请求码获取方法，获取请求码
				Map<String, Object> map = reqCodeValueGetter.getValues(readBy, info);
				
				
				Object transcode = map.get(IHeaderDataValueGetter.TRANSCODE);
				if(transcode == null) {
					throw new RuntimeException("取请求码失败！！！");
				}
				log.info("请求码器解析的请求码: " + transcode);
				if(!handlerMap.containsKey(transcode)) {
					throw new RuntimeException("请求码无对应的处理类！！！");
				}
				
				//生成解包需要的info
				MessagePackerInfo reqPackerInfo = cerateMessagePackerInfo(dom, reqMessagePackerVo, "req");
				
				//解包 生成Vo
				Serializable reqData = messagePacker.unpack(readBy, reqPackerInfo);
				//根据请求码 获取处理器类
				IReqResolver resolver = (IReqResolver) Class.forName((String) handlerMap.get(transcode)).newInstance();
				Object resp = null;
				Serializable retData = null;
				try {
					//处理器类调用处理方法
					retData = resolver.execute(reqData);
					MessagePackerInfo respPackerInfo = cerateMessagePackerInfo(dom, respMessagePackerVo, "resp");
					//打包
					resp = messagePacker.pack(retData, respPackerInfo);
					log.info("响应报文处理器处理返回的数据包: " + resp);
				} catch (Exception e) {
					if(retData != null) {
						MessagePackerInfo expPackerInfo = cerateMessagePackerInfo(dom, expMessagePackerVo, "exp");
						//打包
						resp = messagePacker.pack(retData, expPackerInfo);
						log.info("异常报文处理器处理返回的数据包: " + resp);
					}
				}
				
				ServerWrite write = new ServerWrite();
				write.write(client,resp);
				
			} catch (IOException e) {
				throw new RuntimeException("读取数据失败",e);
			} catch (InstantiationException e) {
				throw new RuntimeException("创建交易码处理器类失败",e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("创建交易码处理器类失败",e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("找不到交易码对应的处理类",e);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private MessagePackerInfo cerateMessagePackerInfo(JrBoundXmlHandler dom,Class<?> clz,String voType) {
			String type = dom.getReqCodeType(id);
			MessagePackerInfo packerInfo = new MessagePackerInfo();
			packerInfo.setDefaultEncoding(defaultCharset);
			//if("json".equals(type)) {
				packerInfo.setClz(clz);
			//}
			if("tem".equals(type)) {
				packerInfo.setItems(dom.getTemMessagePackerVoInfo(id, voType));
			}
			return packerInfo;
		}
	}
	
	
	class ServerWrite {
		public void write(SocketChannel client, Object resp){
			String size = cal(resp);
			try {
				String respData = size + new String(((byte[])resp),defaultCharset);
				ByteBuffer bb = ByteBuffer.wrap(respData.getBytes(defaultCharset));
				while(bb.hasRemaining()) {
					client.write(bb);
				}
				log.info("写给客户端: " + client + " 的数据包: " + respData);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("编码设置错误", e);
			} catch (IOException e) {
				throw new RuntimeException("写数据失败", e);
			} finally {
				if(client != null) {
					try {
						client.close();
						log.info("客户端关闭: " + client);
					} catch (IOException e) {
						throw new RuntimeException("关闭客户端通道失败", e);
					}
				}
			}
		}
		public String cal(Object resp) {
			String ret = "0000";
			if(resp instanceof byte[]){
				String lg = String.valueOf(((byte[])resp).length);
				switch(lg.length()) {
				case 1:
					ret = "000" + lg;
					break;
				case 2:
					ret = "00" + lg;
					break;
				case 3:
					ret = "0" + lg;
					break;
				default :
					throw new RuntimeException("数据包长度异常");
				}
			}else {
				throw new RuntimeException("数据包格式异常");
			}
			return ret;
		}
	}
}