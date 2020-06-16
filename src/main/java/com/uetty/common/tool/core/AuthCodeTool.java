package com.uetty.common.tool.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/**
 * 验证码生成
 */
@SuppressWarnings("unused")
public class AuthCodeTool {
		//生成验证码原始数据
		private static final String CODE_LIST = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
		private final String[] fonts = {"Times New Roman","Axure Handwriting","Agency FB","Gill Sans","Comic Sans MS","Californian FB","Eras ITC","Lucida Bright","Bell MT","Ebrima"};
		//验证码高度
		private static final int HEIGHT = 20;
		//默认验证码字符数
		private static final int FONT_NUM = 4;
		//验证码宽度
		private final int width;
		//验证码字符数
		private final int iNum;
		//验证码原始数据列表
		private final String codeList;
		//是否画验证码背景
		private boolean drawBgFlag = false;
		
		//rbg值
		private int rBg = 0;
		private int gBg = 0;
		private int bBg = 0;
		
		//构造函数
		public AuthCodeTool() {
		this.width = 13 * FONT_NUM + 12;
		this.iNum = FONT_NUM;
		this.codeList = CODE_LIST;
		}
		/**
		 * 生成验证码方法
		 * @return 验证码
		 */
		public Map<String,Object> createRandImage(){
			//图片对象
			BufferedImage image = new BufferedImage(width, HEIGHT,
			BufferedImage.TYPE_INT_RGB);
			
			//制图对象
			Graphics g = image.getGraphics();
			
			//随机数对象
			Random random = new Random();
			
			//如果画背景,就不生成噪点
			if ( drawBgFlag ){
				g.setColor(new Color(rBg,gBg,bBg));
				g.fillRect(0, 0, width, HEIGHT);
			}else{
				g.setColor(getRandColor(200, 250));
				g.fillRect(0, 0, width, HEIGHT);
				//画噪点线
				for (int i = 0; i < 155; i++) {
					g.setColor(getRandColor(140, 200));
					int x = random.nextInt(width);
					int y = random.nextInt(HEIGHT);
					int xl = random.nextInt(12);
					int yl = random.nextInt(12);
					g.drawLine(x, y, x + xl, y + yl);
					}
			}
			//设置绘图字体
	
			//验证码字符串
			StringBuilder sRand= new StringBuilder();
			//绘画字体
			for (int i=0;i<iNum;i++){
				g.setFont(getFont(random.nextInt(9),random.nextInt(2),random.nextInt(7)));
				int rand=random.nextInt(codeList.length());
				//随即生成验证码
				String strRand=codeList.substring(rand,rand+1);
				sRand.append(strRand);
				g.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
//				  g.translate(x-1, y+3);              
			        //   旋转文本   
		        //如果在画字之前旋转图片 
//				int degree = 0;
//	            if(i!=2){        
//	                   int maxDegree=random.nextInt(2); 
//	                   if(maxDegree==0) 
//	                    maxDegree=0; 
//	                   else maxDegree=305; 
//	           degree=random.nextInt(45)+maxDegree;            
//	            }   else degree=0;  
//	            
//	            //   旋转文本   
//	             //特别需要注意的是,这里的画笔已经具有了上次指定的一个位置,所以这里指定的其实是一个相对位置 
//	            Graphics2D g2d = (Graphics2D) g.create();                      
//	            g2d.translate(i-1, i+3);              
//	            g2d.rotate(degree* Math.PI / 180);  
//	            g2d.drawString(strRand,13*i+6,16);
				  g.drawString(strRand,13*i+6,16);
			}
			g.dispose();
			Map<String,Object> map= new HashMap<>();
			map.put("authcodeImage", image);
			map.put("authcode", sRand.toString());
		/*	try{
				ImageIO.write(image, "JPEG", response.getOutputStream());
			}catch(IOException e){
				e.printStackTrace();
			}*/
			//返回验证码
			return map;
		}
		private Font getFont(int nextInt,int style,int size) {
			return new Font(fonts[nextInt],style, 12+size);
		}
		/**
		 * 设置背景颜色
		 * @param r 红色值
		 * @param g 绿色值
		 * @param b 蓝色值
		 */
		public void setBgColor(int r,int g,int b){
			drawBgFlag = true;
			this.rBg = r;
			this.gBg = g;
			this.bBg = b;
		}
		
		/**
		 * 生成随机数
		 * @param fc 设置颜色范围（小值）
		 * @param bc 设置颜色范围（大值）
		 * @return 颜色对象
		 */
		private Color getRandColor(int fc, int bc) {
			Random random = new Random();
			if (fc > 255)
			fc = 255;
			if (bc > 255)
			bc = 255;
			int r = fc + random.nextInt(bc - fc);
			int g = fc + random.nextInt(bc - fc);
			int b = fc + random.nextInt(bc - fc);
			return new Color(r, g, b);
		}
}
