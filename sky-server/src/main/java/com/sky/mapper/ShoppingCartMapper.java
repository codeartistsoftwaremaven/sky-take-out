package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Select("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart cart);

    @Insert("insert into shopping_cart(name, user_id, dish_id, setmeal_id, dish_flavor, amount, image,create_time) " +
            "values(#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);
}
