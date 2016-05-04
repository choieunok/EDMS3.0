package kr.co.exsoft.eframework.handler;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/***
 * SqlSessionFactory 싱글톤 패턴 적용
 * @author 패키지 개발팀
 * @since 2014.08.27
 * @version 3.0
 *
 */
public class SingletonSqlFactory {
	private volatile static SqlSessionFactory uniqueInstance; // volatile로 멀티쓰레드 동기화 safe 함.
	
	private SingletonSqlFactory(){}
	
	public static SqlSessionFactory getInstance(){
		if(uniqueInstance == null){
			synchronized (SingletonSqlFactory.class) { // 동기화 적용
				if(uniqueInstance == null){
					try {
						
						// root경로는 classes인데 톰캣의 경우 ../설정 시 WEB-INF 폴더로 이동 가능하나 JBOSS 이동 불가(classes 폴더 유지)
						Reader reader = Resources.getResourceAsReader("/config/mybatis-application.xml");
						uniqueInstance = new SqlSessionFactoryBuilder().build(reader);
						reader.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return uniqueInstance;
	}
	
}
