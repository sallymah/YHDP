package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpSettleOut;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MemberData {
	private String memId = "";		
	private String agency = "";
	private String industryId = "";
	private String taxContainFlag = "";
		
	public String getMemId() {
		return memId;
	}
	public void setMemId(String memId) {
		this.memId = memId;
	}
	public String getAgency() {
		return agency;
	}
	public void setAgency(String agency) {
		this.agency = agency;
	}
	public String getIndustryId() {
		return industryId;
	}
	public void setIndustryId(String industryId) {
		this.industryId = industryId;
	}
	public String getTaxContainFlag() {
		return taxContainFlag;
	}
	public void setTaxContainFlag(String taxContainFlag) {
		this.taxContainFlag = taxContainFlag;
	}
	
	public void setMapInfo(ResultSet rs) throws SQLException {		
		if ( rs.getString(1) != null )
			memId = (String) rs.getString(1);
		if ( rs.getString(2) != null )
			agency = (String) rs.getString(2);
		if ( rs.getString(3) != null )
			industryId = (String) rs.getString(3);	
		if ( rs.getString(4) != null )
			taxContainFlag = (String) rs.getString(4);	
		
	}

	public String toString( ) {	
		return "[memId: " + memId + " agency: " + agency + 
				" industryId: " + industryId + " taxContainFlag: " + taxContainFlag + "]";
	}
	
	public void remove() {
		memId = "";		
		agency = "";
		industryId = "";
		taxContainFlag = "";
	}
}
