package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTeff;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.packager.GenericPackager;

import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.ISOMsg;
import tw.com.hyweb.util.ISOUtil;

public class Iso8583 {
	
	private static Logger log = Logger.getLogger(Iso8583.class);
	private String isoconfig = "config"+File.separator+"default"+File.separator+"isoconfig.xml";
	private ISOMsg isoMsg;
	private BerTLV berTlv48;
	private BerTLV berTlv60;
	private BerTLV berTlv61;
	private BerTLV berTlv63;
	
	public static void main(String[] args){
		
		String lmsRawData = "02002020000000C1000B88888800000539323031323130303030303739303238303030303133390129FF21027707FF2203000001FF230400001001FF25089851422336844805FF260420991231FF270400000057FF2808773A3132804E52F0FF30080052390010909805FF290720160118113045FF2F06011811304505FF45014CFF3D0103FF500430303030FF57083ECF7BEF27DF7061FF5808339EEF0E9C513E63FF2B0500001000000043FF3828000000000000000026500000010101999912310031000000010000000000000001010199991231010022FF391300000000000000000000000000000000000000E70315D5FC9977AB";
		Iso8583 iso8583 = new Iso8583(lmsRawData);
	}
	
	public Iso8583() {
	}
	public Iso8583(String lmsRawData)
	{
		isoMsg = new ISOMsg();
        isoMsg.setDirection(ISOMsg.INCOMING);
        try {
        	isoMsg.setPackager(new GenericPackager(getIsoconfig()));
			isoMsg.unpack(ISOUtil.hex2byte(lmsRawData));
			
			log.debug(isoMsg.toString());
			
			if (isoMsg.getString(48) != null){
				String ismMsg48 = isoMsg.getString(48);
				berTlv48 = BerTLV.createInstance(ISOUtil.hex2byte(ismMsg48));
				log.debug("berTlv48: "+ berTlv48);
			}
			
			if (isoMsg.getString(60) != null){
				String ismMsg60 = isoMsg.getString(60);
				berTlv60 = BerTLV.createInstance(ISOUtil.hex2byte(ismMsg60));
				log.debug("berTlv60: "+ berTlv60);
			}
			
			if (isoMsg.getString(61) != null){
				String ismMsg61 = isoMsg.getString(61);
				berTlv61 = BerTLV.createInstance(ISOUtil.hex2byte(ismMsg61));
				log.debug("berTlv61: "+ berTlv61);
			}
			
			if (isoMsg.getString(63) != null){
				String ismMsg63 = isoMsg.getString(63);
				berTlv63 = BerTLV.createInstance(ISOUtil.hex2byte(ismMsg63));
				log.debug("berTlv63: "+ berTlv63);
			}
			
        } catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public String getIsoconfig() {
		return isoconfig;
	}
	public void setIsoconfig(String isoconfig) {
		this.isoconfig = isoconfig;
	}
	public ISOMsg getIsoMsg() {
		return isoMsg;
	}
	public void setIsoMsg(ISOMsg isoMsg) {
		this.isoMsg = isoMsg;
	}
	public BerTLV getBerTlv48() {
		return berTlv48;
	}
	public void setBerTlv48(BerTLV berTlv48) {
		this.berTlv48 = berTlv48;
	}
	public BerTLV getBerTlv60() {
		return berTlv60;
	}
	public void setBerTlv60(BerTLV berTlv60) {
		this.berTlv60 = berTlv60;
	}
	public BerTLV getBerTlv61() {
		return berTlv61;
	}
	public void setBerTlv61(BerTLV berTlv61) {
		this.berTlv61 = berTlv61;
	}
	public BerTLV getBerTlv63() {
		return berTlv63;
	}
	public void setBerTlv63(BerTLV berTlv63) {
		this.berTlv63 = berTlv63;
	}
	
}
