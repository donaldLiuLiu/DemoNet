package com.flayway.fl.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FlJrClient {
	
	private String host = "127.0.0.1";
	private int port = 65000;
	private Socket socket;
	
	/*
	 * java socket 连接   接入服务
	 */
	public void semdMsg() {
		socket = new Socket();
		try{
			socket.connect(new InetSocketAddress(host,port));
			
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			
			//String xml = "<root><reqcode>00000</reqcode><reqmsg>ed忠实的粉丝的ed</reqmsg></root>";
			String xml = "{\"reqcode\":\"00000\",\"reqmsg\":\"123忠实的粉丝\"}";
			int recvLen = xml.getBytes("UTF-8").length;
			String recvLenS = (String.valueOf(recvLen));
			switch( recvLenS.length() ) {
			case 1:
				recvLenS = "000"+recvLenS;
				break;
			case 2:
				recvLenS = "00"+recvLenS;
				break;
			case 3:
				recvLenS = "0"+recvLenS;
				break;
			default :
				throw new RuntimeException("传输的数据量不合法");
			}
			//四位数字表示传递数据的字节大小 加 特定格式的数据
			xml = recvLenS + xml;
			os.write(xml.getBytes("UTF-8"));
			os.flush();
			
			int l = -1;
			byte[] by = new byte[1024];
			StringBuffer sb = new StringBuffer();
			while( (l=is.read(by)) != -1 ){
				sb.append(new String(by,0,l));
			}
			System.out.println(sb);
			
			is.close();
			os.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String argv[]) throws Exception {
		FlJrClient client = new FlJrClient();
		client.semdMsg();
	}
	
}
