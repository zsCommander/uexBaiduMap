package org.zywx.wbpalmstar.plugin.uexbaidumap.function;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.plugin.uexbaidumap.EBaiduMapUtils;
import org.zywx.wbpalmstar.plugin.uexbaidumap.EUExBaiduMap;
import org.zywx.wbpalmstar.plugin.uexbaidumap.bean.GeoReverseResultVO;
import org.zywx.wbpalmstar.plugin.uexbaidumap.utils.MLog;

/**
 * 地理编码功能
 *
 * @author waka
 * @version createTime:2016年4月27日 上午10:49:29
 */
public class GeoCoderFunction implements OnGetGeoCoderResultListener {

    private EUExBaiduMap mEUExBaiduMap;

    private GeoCoder mGeoCoder;

    private String mCallbackId;

    /**
     * 构造方法
     *
     * @param uexBaiduMap
     */
    public GeoCoderFunction(EUExBaiduMap uexBaiduMap,String callbackId) {

        mEUExBaiduMap = uexBaiduMap;

        // 第一步，创建地理编码检索实例
        mGeoCoder = GeoCoder.newInstance();

        // 第三步，设置地理编码检索监听者
        mGeoCoder.setOnGetGeoCodeResultListener(this);

        this.mCallbackId =callbackId;
    }

    // 第二步，创建地理编码检索监听者

    /**
     * 地理编码
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有检索到结果
            jsonLatLngCallback(null, EBaiduMapUtils.MAP_FUN_CB_GEOCODE_RESULT);
            return;
        }
        // 获取地理编码结果
        jsonLatLngCallback(result.getLocation(), EBaiduMapUtils.MAP_FUN_CB_GEOCODE_RESULT);

        destory();
    }

    /**
     * 反向地理编码
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有找到检索结果
            jsonAddressCallback(null, EBaiduMapUtils.MAP_FUN_CB_REVERSE_GEOCODE_RESULT);
            return;
        }
        // 获取反向地理编码结果
        jsonAddressCallback(result, EBaiduMapUtils.MAP_FUN_CB_REVERSE_GEOCODE_RESULT);

        destory();
    }

    /**
     * 发起地理编码检索
     *
     * @param city
     * @param address
     */
    public void geocode(String city, String address) {

        MLog.getIns().i("city = " + city);
        MLog.getIns().i("address = " + address);

        mGeoCoder.geocode(new GeoCodeOption().city(city).address(address));
    }

    /**
     * 发起反地理编码检索
     *
     * @param longitude
     * @param latitude
     */
    public void reverseGeoCode(double longitude, double latitude) {

        MLog.getIns().i("longitude = " + longitude);
        MLog.getIns().i("latitude = " + latitude);

        LatLng latLng = new LatLng(latitude, longitude);
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
    }

    /**
     * 地理编码给前端回调
     *
     * @param point
     * @param header
     */
    private void jsonLatLngCallback(LatLng point, String header) {
        if (mEUExBaiduMap != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                if (point != null) {
                    jsonObject.put(EBaiduMapUtils.MAP_PARAMS_JSON_KEY_LNG, Double.toString(point.longitude));
                    jsonObject.put(EBaiduMapUtils.MAP_PARAMS_JSON_KEY_LAT, Double.toString(point.latitude));
                }
                if (null != mCallbackId) {
                    mEUExBaiduMap.callbackToJs(Integer.parseInt(mCallbackId), false, 0,jsonObject);
                }else{
                    String js = EUExBaiduMap.SCRIPT_HEADER + "if(" + header + "){" + header + "('" + jsonObject.toString() + "');}";
                    mEUExBaiduMap.onCallback(js);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 反地理编码给前端回调
     *  @param address
     * @param header
     */
    private void jsonAddressCallback(ReverseGeoCodeResult address, String header) {
        if (mEUExBaiduMap != null) {
            GeoReverseResultVO reverseResultVO=new GeoReverseResultVO();
            if (address!=null){
                reverseResultVO.address=address.getAddress();
                reverseResultVO.city=address.getAddressDetail().city;
                reverseResultVO.district=address.getAddressDetail().district;
                reverseResultVO.province=address.getAddressDetail().province;
                reverseResultVO.street=address.getAddressDetail().street;
                reverseResultVO.streetNumber=address.getAddressDetail().streetNumber;
            }else{
                reverseResultVO.address=null;
            }
            if (null != mCallbackId) {
                mEUExBaiduMap.callbackToJs(
                        Integer.parseInt(mCallbackId),
                        false,
                        address==null?1:0,
                        DataHelper.gson.toJsonTree(reverseResultVO));
            } else {
                String js = EUExBaiduMap.SCRIPT_HEADER + "if(" + header + "){" + header + "('" + DataHelper.gson.toJson(reverseResultVO) + "');}";
                mEUExBaiduMap.onCallback(js);
            }

        }
    }

    /**
     * destory
     */
    private void destory() {

        if (mGeoCoder != null) {

            // 第五步，释放地理编码检索实例
            mGeoCoder.destroy();
            mGeoCoder = null;
            mEUExBaiduMap = null;
        }
    }
}
