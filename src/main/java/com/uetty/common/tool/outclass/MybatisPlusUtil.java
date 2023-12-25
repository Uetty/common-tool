//package com.uetty.common.tool.outclass;
//
//import com.baomidou.mybatisplus.core.enums.SqlMethod;
//import com.baomidou.mybatisplus.core.mapper.Mapper;
//import com.baomidou.mybatisplus.core.metadata.TableInfo;
//import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
//import com.baomidou.mybatisplus.core.toolkit.Assert;
//import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
//import com.baomidou.mybatisplus.core.toolkit.Constants;
//import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.binding.MapperMethod;
//import org.apache.ibatis.logging.Log;
//import org.apache.ibatis.logging.LogFactory;
//import org.apache.ibatis.session.ExecutorType;
//import org.apache.ibatis.session.SqlSession;
//import org.apache.ibatis.session.SqlSessionFactory;
//
//import java.util.Collection;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//
//@Slf4j
//public class MybatisPlusUtil {
//
//    private static Log ibatisLog = LogFactory.getLog(MybatisPlusUtil.class);
//
//    private static SqlSessionFactory sqlSessionFactory;
//
//    private SqlSessionFactory getSqlSessionFactory(){
//        if (sqlSessionFactory == null) {
//            sqlSessionFactory = SpringElementPeeper.getBean(SqlSessionFactory.class);
//        }
//        return sqlSessionFactory;
//    }
//
//    protected static String getSqlStatement(Class<?> mapperClass, SqlMethod sqlMethod) {
//        return SqlHelper.getSqlStatement(mapperClass, sqlMethod);
//    }
//
//    public static <T, K extends T> boolean executeBatch(Class<T> entityClass, Collection<K> list, int batchSize, BiConsumer<SqlSession, K> consumer) {
//        if (list.size() == 0) {
//            return true;
//        }
//        return SqlHelper.executeBatch(entityClass, ibatisLog, list, batchSize, consumer);
//    }
//
//    public static <T> boolean updateBatchById(Class<?> mapperClass, Class<T> entityClass, Collection<T> entityList, int batchSize) {
//        if (entityList.size() == 0) {
//            return false;
//        }
//        String sqlStatement = getSqlStatement(mapperClass, SqlMethod.UPDATE_BY_ID);
//        return executeBatch(entityClass, entityList, Math.min(batchSize, entityList.size()), (sqlSession, entity) -> {
//            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
//            param.put(Constants.ENTITY, entity);
//            sqlSession.update(sqlStatement, param);
//        });
//    }
//
//    public static <T> boolean saveOrUpdateBatchById(Class<?> mapperClass, Class<T> entityClass, Collection<T> entityList) {
//        return saveOrUpdateBatchById(mapperClass, entityClass, entityList, entityList.size());
//    }
//
//    public static <T> boolean saveOrUpdateBatchById(Class<?> mapperClass, Class<T> entityClass, Collection<T> entityList, int batchSize) {
//        if (entityList.size() == 0) {
//            return false;
//        }
//        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
//        Assert.notNull(tableInfo, "error: can not execute. because can not find cache of TableInfo for entity!");
//        String keyProperty = tableInfo.getKeyProperty();
//        Assert.notEmpty(keyProperty, "error: can not execute. because can not find column for id from entity!");
//        return SqlHelper.saveOrUpdateBatch(entityClass, mapperClass, ibatisLog, entityList, batchSize, (sqlSession, entity) -> {
//            Object idVal = ReflectionKit.getFieldValue(entity, keyProperty);
//            return StringUtils.checkValNull(idVal)
//                    || CollectionUtils.isEmpty(sqlSession.selectList(getSqlStatement(mapperClass, SqlMethod.SELECT_BY_ID), entity));
//        }, (sqlSession, entity) -> {
//            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
//            param.put(Constants.ENTITY, entity);
//            sqlSession.update(getSqlStatement(mapperClass, SqlMethod.UPDATE_BY_ID), param);
//        });
//    }
//
//    public static <T> boolean insertBatch(Class<?> mapperClass, Class<T> entityClass, Collection<T> entityList) {
//        String sqlStatement = getSqlStatement(mapperClass, SqlMethod.INSERT_ONE);
//        return executeBatch(entityClass, entityList, entityList.size(), (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
//    }
//
//    public static <T> boolean insertBatch(Class<?> mapperClass, Class<T> entityClass, Collection<T> entityList, int maxBatchSize) {
//        String sqlStatement = getSqlStatement(mapperClass, SqlMethod.INSERT_ONE);
//        return executeBatch(entityClass, entityList, Math.min(maxBatchSize, entityList.size()), (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
//    }
//
//    public static <T> boolean updateBatchById(Class<?> mapperClass, Class<T> entityClass, Collection<T> entityList) {
//        return updateBatchById(mapperClass, entityClass, entityList, entityList.size());
//    }
//
//    public static <T> boolean updateBatchById(Class<?> mapperClass, Collection<T> entityList) {
//        if (entityList.size() == 0) {
//            return false;
//        }
//        Class<T> entityClass = null;
//        for (T t1 : entityList) {
//            entityClass = (Class<T>) t1.getClass();
//            break;
//        }
//        String sqlStatement = getSqlStatement(mapperClass, SqlMethod.UPDATE_BY_ID);
//        return executeBatch(entityClass, entityList, entityList.size(), (sqlSession, entity) -> {
//            MapperMethod.ParamMap<T> param = new MapperMethod.ParamMap<>();
//            param.put(Constants.ENTITY, entity);
//            sqlSession.update(sqlStatement, param);
//        });
//    }
//
//    /**
//     * 批量执行（缺点：脱离了原先的事务，只适合单个执行）
//     */
//    public static <T, K extends Mapper<T>> void batchVoidExecute(Class<K> clazz, Consumer<K> consumer) {
//        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
//
//            K mapper = sqlSession.getMapper(clazz);
//
//            consumer.accept(mapper);
//
//            sqlSession.flushStatements();
//            sqlSession.commit();
//        }
//    }
//
//}
