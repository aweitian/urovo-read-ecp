package cn.wx.myapplication;

import android.device.DeviceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class OtgUtils {

    public static final String NODE_53_1 = "/sys/devices/soc/c170000.serial/pogo_uart";
    public static final String NODE_53_2 = "/sys/devices/virtual/Usb_switch/usbswitch/function_otg_en";

    public static final String NODE_53C_1 = "/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_pogo_5v_ctrl/enable";
    public static final String NODE_53C_2 = "/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_otg_en_ctrl/enable";

    /**
     * 模块上电
     *
     * @param enable
     * @return
     */
    public static boolean set53CGPIOEnabled(boolean enable) {
        String projectName = new DeviceManager().getSettingProperty("pwv.project");
        Log.d("ubx","projectName:" + projectName);
        FileOutputStream node_1 = null;
        FileOutputStream node_2 = null;
        byte[] open_one = new byte[]{0x31};
        byte[] close = new byte[]{0x30};
        try {
            Log.d("ubx","enable:" + enable);
            if (TextUtils.equals(projectName, "SQ53Q") || TextUtils.equals(projectName, "SQ53")) { //53上电
                byte[] open_two = new byte[]{0x32};
                node_1 = new FileOutputStream(NODE_53_1);
                node_1.write(enable ? open_one : close);
                node_2 = new FileOutputStream(NODE_53_2);
                node_2.write(enable ? open_two : close);
            } else { //53C
                node_1 = new FileOutputStream(NODE_53C_1);
                node_1.write(enable ? open_one : close);
                node_2 = new FileOutputStream(NODE_53C_2);
                node_2.write(enable ? open_one : close);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (node_1 != null) {
                    node_1.close();
                }
                if (node_2 != null) {
                    node_2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
