package com.atguigu.gmall.cart.service.Impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfo_Mapper;
import com.atguigu.gmall.service.Cart_Service;
import com.atguigu.gmall.service.Manage_Service;
import com.atguigu.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service    //暴露服务
public class Cart_ServiceImpl implements Cart_Service {

    @Autowired  //注入mapper
    private CartInfo_Mapper cartInfoMapper;

    @Reference  //远程调用
    private Manage_Service manageService;

    @Autowired  //redis
    private RedisUtil redisUtil;



    /**
     * 功能：添加商品到购物车
     * 参数分析：
     * userId：必须要有用户id，要知道哪个用户购买的商品
     * skuId、skuNum：根据控制器“/addToCart”接收的表单参数获得（得自己去页面里找）
     */
    @Override
    public void addToCart(String userId, String skuId, Integer skuNum) {
        /**
         *  1、先根据userId、skuId，查询一下购物车中是否有相同的商品，
         *  2、如果有则数量相加；如果没有，直接添加到数据库!
         *  3、更新缓存!
         */

        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //构建购物车的key【user:userId:cart】
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        //sql：select * from CartInfo where userId=？ and skuId=？
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        CartInfo cartInfo_DB = this.cartInfoMapper.selectOne(cartInfo);
        if (cartInfo_DB != null){   //如果：DB中skuNum=2，页面传递来的skuNum=1
            //购物车中有相同商品
            //数量相加
            cartInfo_DB.setSkuNum(cartInfo_DB.getSkuNum()+skuNum);  //数据库的+页面传递的
            //初始化实时价格（刚加进购物车时，实时价格=购物车价格）skuPrice=cartPrice
            cartInfo_DB.setSkuPrice(cartInfo_DB.getCartPrice());
            //设置完值后，要更新cartInfo_DB表（skuNum=2+1=3）
            cartInfoMapper.updateByPrimaryKeySelective(cartInfo_DB);
            //同步缓存
            //jedis.hset(cartKey, userId, JSON.toJSONString(cartInfo_DB));     //有重复代码
        }else {
            //购物车中没有相同商品----->>第一次添加到购物车（到DB）
            //CartInfo数据来源，来源于商品详情页面，也就是来源于SkuInfo
            SkuInfo skuInfo = this.manageService.getSkuInfo_bySkuId(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            //插入CartInfo表
            this.cartInfoMapper.insertSelective(cartInfo1);
            //同步缓存
            //jedis.hset(cartKey, userId, JSON.toJSONString(cartInfo1));    //有重复代码
            cartInfo_DB=cartInfo1;  //（减少重复代码）
        }
        //放入缓存
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfo_DB));    //放入缓存（减少重复代码）

        //购物车需要设置过期时间吗？ 一般不设置过期时间。
        //如果非要设置呢？  那么，可以设置购物车过期时间=用户的过期时间
        //构建用户的key【user:userId:info】,然后获取key的过期时间
        String userKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long userKey_OutTime = jedis.ttl(userKey);
        jedis.expire(cartKey, userKey_OutTime.intValue());

        //关闭jedis连接
        jedis.close();
    }


    //登录状态下查询购物车。从redis中取，或者从数据库中取
    /**思路：
     * 1.如果购物车在缓存中存在 看购物车!
     * 2.如果缓存中不存在，看数据库，并将数据放入缓存!
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        //创建一个存储"CartInfo"的集合
        List<CartInfo> cartInfoList = new ArrayList<>();

        //---从缓存中获取数据----
        //获取jedis
        Jedis jedis = this.redisUtil.getJedis();
        //构建购物车的key【user:userId:cart】
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //从key中获取数据
        //  jedis.hgetAll(cartKey); 返回map。 key=field，value=cartInfo字符串
        List<String> cartInfoStr_List = jedis.hvals(cartKey);// 返回List集合 String = cartInfo 字符串
        if (cartInfoStr_List!=null && cartInfoStr_List.size()>0){
            for (String cartInfoStr : cartInfoStr_List) {
                //把cartInfoStr转换成CartInfo对象，并添加到集合。遍历结束后返回
                cartInfoList.add(JSON.parseObject(cartInfoStr, CartInfo.class));
            }
            // 查看的时候应该做排序！真实项目按照更新时间 {模拟按照id 进行排序}
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // 定义比较规则：s1 = abc；s2=abcd。。（好像是降序）
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else {
            //---从DB中获取数据---排序order by---，并添加到缓存
            cartInfoList = getCartInfoDB_toCache(userId);
            return cartInfoList;
        }
    }

    /**
     * 功能：合并购物车（cookie购物车和DB购物车）
     */
    @Override
    public List<CartInfo> mergeCart_CookieAndDB(List<CartInfo> cartInfoList_CK, String userId) {
        /*
            未登录：    33 1 ，34 2
            登录：      34 1 ，36 1
            匹配之后：  33 1 ，34 3
            合并 ：    33 1 ，34 3，36 1
         */
        //获取DB购车数据
        List<CartInfo> cartInfoList_DB = this.cartInfoMapper.getCartListWithSkuPrice(userId);
        //开始合并（合并条件：skuId相同）
        for (CartInfo cartInfo_CK : cartInfoList_CK) {
            //定义一个boolean类型变量，默认为false。区分是否需要合并。
            boolean isMerge = false;
            for (CartInfo cartInfo_DB : cartInfoList_DB) {
                //cookie的skuId==DB的skuId---->>skuNum相加
                if (cartInfo_CK.getSkuId().equals(cartInfo_DB.getSkuId())){
                    //合并（cookie+DB）
                    cartInfo_DB.setSkuNum(cartInfo_CK.getSkuNum()+cartInfo_DB.getSkuNum());
                    //设值后，更新DB数据
                    this.cartInfoMapper.updateByPrimaryKeySelective(cartInfo_DB);
                    // 表示走完一遍，把"isMerge"设置为true。
                    isMerge = true;
                }
            }
            //isMerge为true【cookie的skuId!=DB的skuId】
            if (!isMerge){
                //未登录用户对象添加到DB
                //将用户id赋值给未登录对象
                cartInfo_CK.setUserId(userId);
                this.cartInfoMapper.insertSelective(cartInfo_CK);
            }
        }
        //经过上面的合并（更新、插入），
        List<CartInfo> cartInfoList = getCartInfoDB_toCache(userId);

        /**
         * 功能：去结算时，合并勾选状态（未登录+登录）。以cookie为基准
         */
        for (CartInfo cartInfo_DB : cartInfoList) {
            for (CartInfo cartInfo_CK : cartInfoList_CK) {
                if (cartInfo_DB.getSkuId().equals(cartInfo_CK.getSkuId())){ //skuId相同
                    //以cookie为基准
                    if ("1".equals(cartInfo_CK.getIsChecked())){
                        //将DB的购物车单选框选中状态，设置为cookie的选中状态
                        cartInfo_DB.setIsChecked(cartInfo_CK.getIsChecked());
                        //设值后，更新redis中的isChecked
                        checkCart(cartInfo_DB.getSkuId(),cartInfo_DB.getIsChecked(),userId);
                    }
                }
            }
        }
        //最终将合并后的数据返回
        return cartInfoList;
    }

    /**
     *  登录状态。功能：购物车勾选状态
     */
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        /*
            1.  获取Jedis 客户端
            2.  获取购物车
            3.  直接修改skuId 商品的勾选状态 isChecked
            4.  写回购物车
-------------------------------------------------------------------------
            5.  新建一个购物车来存储勾选的商品！
         */
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //构建购物车的key【user:userId:cart】
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //获取数据【为什么使用hget？因为CartInfo有很多个。但通过单选框，一次性只能选一个CartInfo】
        String cartInfoJSON = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJSON, CartInfo.class);
        //为"isChecked"赋值
        cartInfo.setIsChecked(isChecked);
        //缓存更新（重新设置）
        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));

        //为什么要新建一个新key？？方便对购物车进行结算的时候，我可以直接取缓存中被选择的商品！！！
        //----------新建一个key【user:userId:checked】，用于缓存选中的商品
        String cartKey_Checked = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(isChecked)){ //【经过观察：cartList.html页面的商品单选框被选中，会传递"isChecked==1"】
            //页面某单选框被选中，放缓存【缓存所有被选中的商品】
            jedis.hset(cartKey_Checked,skuId,JSON.toJSONString(cartInfo));
        }else {
            //取消某单选框勾选【取消缓存商品】
            jedis.hdel(cartKey_Checked,skuId);
        }
        jedis.close();
    }

    //根据用户id，获取缓存中，被选中的购物车
    @Override
    public List<CartInfo> getCartListChecked_onRedis(String userId) {
        //存储购物车的集合
        List<CartInfo> cartInfoList = new ArrayList<>();

        Jedis jedis = this.redisUtil.getJedis();
        //1、构建被选中的购物车key【user:userId:checked】
        String cartKey_Checked = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        //2、获取缓存中所有被选中的购物车的值
        List<String> cartInfoListJSON = jedis.hvals(cartKey_Checked);
        //3、将购物车的值放进集合，并返回
        if (cartInfoListJSON!=null && cartInfoListJSON.size()>0){
            for (String cartInfoJSON : cartInfoListJSON) {
                cartInfoList.add(JSON.parseObject(cartInfoJSON,CartInfo.class));
            }
        }
        return cartInfoList;
    }



    //根据userId，查询用户的购物车数据｛含skuPrice实时价格｝,并放进缓存
    public List<CartInfo> getCartInfoDB_toCache(String userId) {
        //sql：select * from CartInfo where userId=？无意义，因为查询不到skuPrice实时价格
        //如果想查到实时价格，应该两表关联查询（CartInfo、SkuInfo）
        List<CartInfo> cartInfoList = this.cartInfoMapper.getCartListWithSkuPrice(userId);
        if (cartInfoList==null || cartInfoList.size()==0){
            //查不到数据---返回空
            return null;
        }else {
            //查得到数据----放进缓存，后返回集合
            Jedis jedis = redisUtil.getJedis();
            //构建购物车的key【user:userId:cart】
            String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
            for (CartInfo cartInfo : cartInfoList) {
                //放进缓存。hash【user:userId:cart】，key【skuId】，value【cartInfo】
                jedis.hset(cartKey, cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            jedis.close();
            return cartInfoList;
        }
    }
}
