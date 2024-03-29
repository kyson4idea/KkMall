package com.kyson.mall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class Cart {


    private Integer countNum;   //商品数量

    private Integer countType;  //商品类型数量

    private BigDecimal totalAmount;    //商品总价

    private BigDecimal reduce = new BigDecimal("0.00");

    List<CartItem> items;

    public Integer getCountNum()
    {

        Integer count = 0;
        if (items != null && items.size() > 0) {

            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType()
    {

        Integer count = 0;
        if (items != null && items.size() > 0) {

            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount()
    {

        BigDecimal amout = new BigDecimal("0");

        //计算购物项总价
        if (items != null && items.size() > 0) {

            for (CartItem item : items) {
                if (item.getCheck()) {
                    amout = amout.add(item.getTotalPrice());
                }
            }
        }

        //减去优惠价格
        BigDecimal subtract = amout.subtract(getReduce());

        return subtract;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {

        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce()
    {

        return reduce;
    }

    public void setReduce(BigDecimal reduce)
    {

        this.reduce = reduce;
    }

    public List<CartItem> getItems()
    {

        return items;
    }

    public void setItems(List<CartItem> items)
    {

        this.items = items;
    }
}
