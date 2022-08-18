package com.su.gulimall.order.dao;

import com.su.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author sukun
 * @email 1477264431@qq.com
 * @date 2022-08-18 21:27:17
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
