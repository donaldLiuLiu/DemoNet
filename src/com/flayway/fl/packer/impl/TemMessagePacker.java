package com.flayway.fl.packer.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.flayway.fl.packer.IMessagePacker;
import com.flayway.fl.packer.MessagePackerInfo;
import com.flayway.fl.packer.MessagePackerItem;
import com.flayway.fl.test.vo.ReqVoVo;
import com.flayway.fl.test.vo.TemReqVo;
import com.flayway.fl.util.JrBoundXmlHandler;

public class TemMessagePacker implements IMessagePacker {
	
	@Override
	public Object pack(Serializable param, MessagePackerInfo info) {
		//按顺序 把每个字段按指定长度字节化
		Field[] fls = param.getClass().getDeclaredFields();
		ByteArrayBuilder bab = new ByteArrayBuilder();
		for(Field fl : fls) {
			fl.setAccessible(true);
			
			String key = fl.getName();
			List<MessagePackerItem> items = info.getItems();
			MessagePackerItem itemRed = null;
			for(MessagePackerItem item : items) {
				if(item.getName().equals(key)) {
					itemRed = item;
				}
			}
			if(itemRed == null) {
				throw new RuntimeException("vo中的属性未定义打包信息,无法打包");
			}
			
			
			if("param".equals(itemRed.getType())) {
				int length = Integer.parseInt(itemRed.getLength());
				byte[] by = new byte[length];
				
				Object vl = null;
				try {
					vl = fl.get(param);
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
				System.arraycopy(((String)vl).getBytes(), 0, by, 0, ((String)vl).getBytes().length);
				bab.write(by);
			} else if("list".equals(itemRed.getType())) {
				String lengthref = itemRed.getLength();
				Field lengthinfo = null;
				for(Field fltmp : fls) {
					if(fltmp.getName().equals(lengthref)) {
						lengthinfo = fltmp;
						break;
					}
				}
				if(lengthinfo == null) {
					throw new RuntimeException("打包list时，无法确定list大小");
				}
				int size = 0;
				try {
					size = Integer.parseInt((String) lengthinfo.get(param));
				} catch (Exception e) {
					throw new RuntimeException("获取属性值失败" + e.getMessage());
				}
				for(int i=0;i<size;i++) {
					List<?> lvl = null;
					try {
						lvl = (List<?>) fl.get(param);
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
					Object lvli = lvl.get(i);
					Field[] lvlfls = lvli.getClass().getDeclaredFields();
					for(Field lvlfl : lvlfls) {
						lvlfl.setAccessible(true);
						
						try {
							Object lvlvl = lvlfl.get(lvli);
							
							Map<Object, Object> map = itemRed.getMap();
							int length = Integer.parseInt((String) map.get(lvlfl.getName()));
							
							byte[] lvby = new byte[length];
							System.arraycopy(((String)lvlvl).getBytes(), 0, lvby, 0, ((String)lvlvl).getBytes().length);
							
							bab.write(lvby);
							
						} catch (Exception e) {
							throw new RuntimeException(e.getMessage());
						}
					}
				}
			} else if("pojo".equals(itemRed.getType())) {
				
			} else if("map".equals(itemRed.getType())) {
				
			}
		}
		return bab.toByteArray();
	}
	
	@Override
	public Serializable unpack(Object param, MessagePackerInfo info) {
		Serializable ret = null;
		if(param instanceof byte[]) {
			byte[] byParam = (byte[]) param;
			int currentPos = 0;
			Class<?> clz = info.getClz();
			try {
				ret = (Serializable) clz.newInstance();
			} catch (Exception e1) {
				throw new RuntimeException("解包时初始化Vo失败");
			}		
			List<MessagePackerItem> items = info.getItems();
			//遍历ret中的每一个属性(对于list属性，前面还必须有一个属性指定该list大小要先被解析)
			Field[] fls = clz.getDeclaredFields();
			for(Field fl : fls) {
				fl.setAccessible(true);
				String key = fl.getName();
				MessagePackerItem itemRed = null;
				for(MessagePackerItem item : items) {
					if(item.getName().equals(key)) {
						itemRed = item;
					}
				}
				if(itemRed == null) {
					throw new RuntimeException("vo中的属性未定义解包信息,无法解包");
				}
				if("param".equals(itemRed.getType())) {
					
					int length = Integer.parseInt(itemRed.getLength());
					String vl = null;
					try {
						vl = new String(byParam, currentPos, length, info.getDefaultEncoding());
						currentPos += length;  //更新字节数组的位置
					} catch (UnsupportedEncodingException e1) {
						throw new RuntimeException("编码失败" + e1.getMessage());
					}
					try {
						fl.set(ret, vl);
					} catch (Exception e) {
						throw new RuntimeException("设置属性值失败");
					}
				} else if("list".equals(itemRed.getType())) {
					String lengthref = itemRed.getLength();
					Field lengthinfo = null;
					for(Field fltmp : fls) {
						if(fltmp.getName().equals(lengthref)) {
							lengthinfo = fltmp;
							break;
						}
					}
					if(lengthinfo == null) {
						throw new RuntimeException("解包list时，无法确定list大小");
					}
					int size = 0;
					try {
						size = Integer.parseInt((String) lengthinfo.get(ret));
					} catch (Exception e) {
						throw new RuntimeException("获取属性值失败" + e.getMessage());
					}
					List<Object> list = new ArrayList<Object>();
					for(int i=0;i<size;i++) {
						Serializable ins = null;
						try {
							ins = (Serializable) itemRed.getClz().newInstance();
						} catch (Exception e) {
							throw new RuntimeException(e.getMessage());
						}
						for(Field lfl : itemRed.getClz().getDeclaredFields()) {
							lfl.setAccessible(true);
							String lkey = lfl.getName();
							Map<Object, Object> lmap = itemRed.getMap();
							int llength = Integer.parseInt((String) lmap.get(lkey));
							
							String lvl = null;
							try {
								lvl = new String(byParam, currentPos, llength, info.getDefaultEncoding());
								currentPos += llength;  //更新字节数组的位置
								lfl.set(ins, lvl);
							} catch (Exception e1) {
								throw new RuntimeException(e1.getMessage());
							}
						}
						list.add(ins);
					}
					try {
						fl.set(ret, list);
					} catch (Exception e) {
						throw new RuntimeException("获取属性值失败");
					}
				} else if("pojo".equals(itemRed.getType())) {
					
					Serializable ins = null;
					try {
						ins = (Serializable) itemRed.getClz().newInstance();
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
					for(Field lfl : itemRed.getClz().getDeclaredFields()) {
						lfl.setAccessible(true);
						String lkey = lfl.getName();
						Map<Object, Object> lmap = itemRed.getMap();
						int llength = Integer.valueOf((String) lmap.get(lkey));
						
						String lvl = null;
						try {
							lvl = new String(byParam, currentPos, llength, info.getDefaultEncoding());
							currentPos += llength;  //更新字节数组的位置
							lfl.set(ins, lvl);
						} catch (Exception e1) {
							throw new RuntimeException(e1.getMessage());
						}
					}
					try {
						fl.set(ret, ins);
					} catch (Exception e) {
						throw new RuntimeException("获取属性值失败");
					}
					
				} else if("map".equals(itemRed.getType())) {
					
					Map<Object, Object> map = new HashMap<Object, Object>();
					for( Entry<Object, Object> entry : itemRed.getMap().entrySet()) {
						int mlength = Integer.valueOf((String) entry.getValue());
						String mvl = null;
						try {
							mvl = new String(byParam, currentPos, mlength, info.getDefaultEncoding());
							currentPos += mlength;  //更新字节数组的位置
							map.put(entry.getKey(), mvl);
						} catch(Exception e) {
							throw new RuntimeException("获取属性值失败");
						}
					}
					try {
						fl.set(ret, map);
					} catch (Exception e) {
						throw new RuntimeException("获取属性值失败");
					}
				}
			}
			
		}else {
			throw new RuntimeException("数据包格式异常");
		}
		return ret;
	}
	
	
	public static void main(String argv[]) throws Exception{
		
		
		TemReqVo vo = new TemReqVo();
		vo.setReqcode("00000");
		vo.setReqmsg("kafjaksdfadsdfsdfsdfsdfds");
		
		vo.setListlength("002");
		List<ReqVoVo> vos = new ArrayList<ReqVoVo>();
		ReqVoVo e1 = new ReqVoVo();
		e1.setReqcodes("00000");
		e1.setReqmsgs("kafjaksdfadsdfsdfsdfsdfd");
		vos.add(e1);
		ReqVoVo e2 = new ReqVoVo();
		e2.setReqcodes("00000");
		e2.setReqmsgs("kafjaksdfadsdfsdfsdfsdfds");
		vos.add(e2);
		
		vo.setVos(vos);
		
		
		JrBoundXmlHandler dom = new JrBoundXmlHandler();
		MessagePackerInfo packerInfo = new MessagePackerInfo();
		packerInfo.setDefaultEncoding("UTF-8");
		packerInfo.setClz(Class.forName("com.flayway.fl.test.vo.TemReqVo"));
		List<MessagePackerItem> info = dom.getTemMessagePackerVoInfo("temBound", "req");
		packerInfo.setItems(info);
		
		TemMessagePacker tem = new TemMessagePacker();
		
		Object rr = tem.pack(vo, packerInfo);
		System.out.println(new String((byte[])rr));
		//[48, 48, 48, 48, 48, 107, 97, 102, 106, 97, 107, 115, 100, 102, 97, 100, 115, 100, 102, 115, 100, 102, 115, 100, 102, 115, 100, 102, 100, 115, 48, 48, 50, 48, 48, 48, 48, 48, 107, 97, 102, 106, 97, 107, 115, 100, 102, 97, 100, 115, 100, 102, 115, 100, 102, 115, 100, 102, 115, 100, 102, 100, 115, 48, 48, 48, 48, 48, 107, 97, 102, 106, 97, 107, 115, 100, 102, 97, 100, 115, 100, 102, 115, 100, 102, 115, 100, 102, 115, 100, 102, 100, 115]
		
		//byte[] b = new byte[1];//b[0] = 32
		
		//String vl = "00000kafjaksdfadsdfsdfsdfsdfd" + new String(b,"gb2312") + "00200000kafjaksdfadsdfsdfsdfsdfds00000kafjaksdfadsdfsdfsdfsdfds";
		
		
		//System.out.println(vl.getBytes().length);
		//System.out.println(new String(vl.getBytes(), 5, 25));
		
		
		Serializable req = tem.unpack(rr, packerInfo);
		
		System.out.println(((TemReqVo)req).getReqcode());
		System.out.println(((TemReqVo)req).getReqmsg());
		
		TemReqVo r = ((TemReqVo)req);
		
		System.out.println(r.getListlength());
		
		System.out.println(r.getVos().size());
		
		System.out.println(r.getVos().get(0).getReqmsgs());
		
	}

}
