package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.validator;

import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.hsm.HsmAdapter;
import tw.com.hyweb.core.service.hsm.HsmResult;
import tw.com.hyweb.core.service.hsm.HsmService;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.Context;
import tw.com.hyweb.online.IValidator;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.svc.cp.online.util.SvcUtil;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.BATCHContext;
import tw.com.hyweb.svc.yhdp.online.CacheTbSysConfig;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.util.LMSTag;
import tw.com.hyweb.svc.yhdp.online.util.LmsDbUtil;
import tw.com.hyweb.svc.yhdp.online.util.LmsUtil;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.ArraysUtil;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

public class MacValidator implements IValidator
{
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(MacValidator.class);

    private HsmAdapter hsmAdapter = new HsmAdapter(HsmService.getHsmService());

    /** mac key - KEY_GROUP_ID(00000000) + KEY_TYPE(1107) + KEY_VERSION(00) */
    private String macKeyID = "SingleTAC";
    private boolean isCheck = true;
    private String ignoreMemberGroup[] = null;

    /* (non-Javadoc)
     * @see tw.com.hyweb.online.IValidator#validate(tw.com.hyweb.online.Context)
     */
    public Context validate(Context ctx)
    {
        String f64 = ctx.getIsoMsg().getString(64);
        String div = f64;
        if (f64!=null)
        {
            LMSContext yhdpCtx = (LMSContext)ctx;
            
            String macApiVer = CacheTbSysConfig.getInstance().getValue("MAC_POS_API_VER", "0");//????????????mac????????????pos api ??????
            boolean isNewMac = false;
            TbTermInfo termInfo;
            try
            {
                termInfo = yhdpCtx.getTermInfo();
                getTsamosn(yhdpCtx);
            }
            catch (SQLException e1)
            {
               logger.error("",e1);
               ctx.setRcode(Rcode.SQL_FAIL);
               return ctx;
            }
            catch (TxException e2)
            {
                // TODO Auto-generated catch block
                logger.error("",e2);
                return ctx;
            }
            
            String posApiVer = termInfo.getPosApiVersion();
            if(!StringUtil.isEmpty(posApiVer))
            {
                if(Integer.valueOf(posApiVer) > Integer.valueOf(macApiVer))//??????pos api??????????????????????????????
                {
                    isNewMac = true;//??????????????????
                }
            }
            
            String raw = ((LMSContext)ctx).getRawData();
            logger.debug("raw data:"+raw);
            logger.debug("f64 len:"+f64.length());
            raw = raw.substring(0,raw.length()-f64.length() - "FF6408".length());
            logger.debug("no f64 raw:"+raw);
            StringBuffer strPad = new StringBuffer();
            int padNum = (8-(raw.length()/2)%8);
            if (padNum>0 && padNum<8)
            {//???1~7???byte ???????????????????????????
                for (int i=0;i<padNum;i++)
                {
                    strPad.append("FF");
                }
            }
            raw = raw+strPad.toString();
            logger.debug("append oxFF raw:"+raw);
            byte[] bRaw = ISOUtil.hex2byte(raw);
            logger.debug("raw:"+ISOUtil.hexString(bRaw));
            byte[] inputData = new byte[]{bRaw[0],bRaw[1],bRaw[2],bRaw[3],bRaw[4],bRaw[5],bRaw[6],bRaw[7]};
            int num = bRaw.length/8;
            for (int i=1;i<num;i++)
            {
                byte[] dest = new byte[8];
                System.arraycopy(bRaw, i*8, dest, 0, 8);
                //logger.debug(i+":"+ISOUtil.hexString(inputData)+" xor "+ISOUtil.hexString(dest));

                for (int j=0;j<8;j++)
                {
                    inputData[j] = (byte)(inputData[j] ^ bRaw[i*8+j]);
                }
            }
            
            /* byte array to hex */
            yhdpCtx.setHexString(ISOUtil.hexString(inputData), termInfo);
            
            /* ?????????pos api?????????????????????*/
            String divData = SvcUtil.genMacDiv(isNewMac, yhdpCtx.getMacPack());
            String inputDataS = SvcUtil.genMacInputData(isNewMac, yhdpCtx.getMacPack());
            String iv = SvcUtil.genMacIv(isNewMac, yhdpCtx.getMacPack());     
            
            logger.debug("iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
            long start = java.util.Calendar.getInstance().getTimeInMillis();
            HsmResult hsmR = hsmAdapter.GenerateTAC_1(macKeyID, divData, inputDataS, iv);
            YHDPUtil.checkMillisTime("MacValidator GenerateTAC_1", 1000, start);
            if (hsmR==null || hsmR.getValue()!=0)
            {
                logger.error("iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
                ctx.setRcode(Rcode.CALL_HSM_FAIL);
                logger.error("hsm return error. hsm ret code="+(hsmR!=null?Integer.toHexString(hsmR.getValue()):"null"));
            }
            else
            {//hsm???API??????????????????00?????????hsmR.getString()????????????NullPointerException???????????????NULL<--???????????????
                String mac = hsmR.getString(0);
                mac = LmsUtil.GenerateTAC_S(((LMSContext)ctx), div, mac, ignoreMemberGroup);
                if (mac!=null && !mac.equalsIgnoreCase(f64))
                {
                    logger.error("iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
                    logger.error("mac error. hsm ret mac:["+mac+"] != edc:["+f64+"]");
                    yhdpCtx.setErrDesc("mac error. hsm ret mac:["+mac+"] != edc:["+f64+"], tsamosn:" +termInfo.getTsamosn());
                    ctx.setRcode(Rcode.MAC_ERROR);
                }
            }
        }
        return ctx;
    }
    /**
     * Gets the mac key id.
     * 
     * @return macKeyID
     */
    public String getMacKeyID()
    {
        return this.macKeyID;
    }
    /**
     * Sets the mac key id.
     * 
     * @param macKeyID ???????????? macKeyID
     */
    public void setMacKeyID(String macKeyID)
    {
        this.macKeyID = macKeyID;
    }
    
    /**
     * Gets the mac key id.
     * 
     * @return macKeyID
     */
    public boolean getIsCheck()
    {
        return this.isCheck;
    }
    /**
     * Sets the mac key id.
     * 
     * @param macKeyID ???????????? macKeyID
     */
    public void setIsCheck(boolean isCheck)
    {
        this.isCheck = isCheck;
    }
    
    public String getTsamosn(LMSContext ctx) throws SQLException, TxException
    {
        BATCHContext bctx = (BATCHContext)ctx;
        //TbMemberInfo acqInfo = getAcquireInfo(bctx);
        //String memGroupId = acqInfo.getMemGroupId();
        BerTLV lmsTlv = bctx.getLMSMsg();
        TbTermInfo termInfo = ctx.getTermInfo();
        String tsamosn = "";
        if(bctx.isEcaMerchId())//?????????????????????tb_term??????
        {
            String samId = lmsTlv.getHexStr(LMSTag.SAMArea);//?????????
            
            /* ??????samId?????????tsamosn */
            String sqlCmd = "select tsam_osn from tb_lptsam where CID = ? AND STATUS ='1' AND ROWNUM = 1";
            Vector<String> parms = new Vector<String>();
            parms.add(samId);//cid??????????????????????????????mem_group_id?????????????????????
            tsamosn = DbUtil.getString(sqlCmd, parms, bctx.getConnection());
            if(StringUtil.isEmpty(tsamosn))
            {
                if(null != termInfo)
                {
                    tsamosn = termInfo.getTsamosn();
                }
                
                if(StringUtil.isEmpty(tsamosn))
                {
                    logger.warn("can't found tsaosn, samId:"+bctx.getSamId());
                }
            }
            else
            {
                termInfo.setTsamosn(tsamosn);//??????tb_lptsam??????
            }
        }
        return tsamosn;
    }
    
    /**
     * ???spring?????????controller???????????????TxCode????????????????????????????????????
     *
     * @param acceptLmsProcCodeL ???????????? acceptLmsProcCode???
     */
    public void setIgnoreMemberGroup(String strIgnoreMemberGroup)
    {
        this.ignoreMemberGroup = ArraysUtil.toStrArray(strIgnoreMemberGroup);
    }
    
    public TbMemberInfo getAcquireInfo(LMSContext yhdpCtx) throws SQLException
    {
        String termId = yhdpCtx.getLmsTerminalId();
        String merchId = yhdpCtx.getLmsMerchantId();
        if (termId!=null)
        {
            if(null == yhdpCtx.getAcquireInfo())
            {
                logger.debug(yhdpCtx.getAcquireInfo());
                TbMemberInfo acquireInfo = LmsDbUtil.getAcquireInfo(true, yhdpCtx.getHostDate(),termId,merchId,yhdpCtx.getConnection());
                yhdpCtx.setAcquireInfo(acquireInfo);
            }
        }
        return yhdpCtx.getAcquireInfo();
    }
}
