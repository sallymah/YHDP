/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.hyweb.gui;

import java.util.Date;
import tw.com.hyweb.util.ISODate;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.Sequencer;


/**
 *
 * @author SamSon
 */
public class InputFn
{

    public InputFn()
    {
    }

    public String genSequence(int len)
    {
        String ret = "";
        ret = Sequencer.getString(getClass().getName(), len);
        return ret;
    }

    public String genRandomNum(int len)
    {
        String ret = "";
        ret = ISOUtil.getRdmNum(3);
        return ret;
    }

    public String genDateTime(String pattern)
    {
        String ret = "";
        ret = ISODate.formatDate(new Date(), pattern);
        return ret;
    }

    public String genTLV()
    {
        String ret = "";
        return ret;
    }

    public String genToken()
    {
        String ret = "";
        return ret;
    }
}
