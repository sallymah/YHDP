/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * ***********************************************
 */

package tw.com.hyweb.core.service.hsm;

import junit.framework.*;
import tw.com.hyweb.core.service.hsm.*;
/**
 * HsmServiceTest
 */
public class HsmServiceTest extends TestCase
{
    private HsmService hsmService = HsmService.getHsmService();
    private HsmResult hsmResult;

    public HsmServiceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        //之前先用KMS建立UNIT_TEST key (ABC, ABC2, 00000000010580)
        //ABC = 1111111111111111-1111111111111111
        //ABC2 = 0102030405060708-1112131415161718
        //HSM =10.10.10.40
    }

    protected void tearDown() throws Exception
    {
        hsmService = null;
        super.tearDown();
    }

    public void testCryptoDES()
    {
        int sessionHandle = HsmInterface.HSM_A;
        int op = HsmInterface.DES_ENCRYPT_ECB;
        String keyId = "ABC";
        int size = 16;
        String data = "22222222222222222222222222222222";
        String iv = "0000000000000000";

        HsmResult actualReturn = hsmService.CryptoDES(sessionHandle, op, keyId, size, data, iv);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
            assertEquals("return value", "950973182317F80B950973182317F80B", actualReturn.getString(0));
        }
    }

    public void testDiversifyDES2_1()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "00000000010580";
        int op1 = HsmInterface.DES_ENCRYPT_ECB;
        int op2 = HsmInterface.DES_ENCRYPT_ECB;
        String divData1 = "12345678EDCBA987EDCBA98712345678";
        String divData2 = "1234567890123456EDCBA9876FEDCBA9";
        String iv1 = "0000000000000000";
        String iv2 = "0000000000000000";

        HsmResult actualReturn = hsmService.DiversifyDES2(sessionHandle, keyId, op1, op2, divData1, divData2, iv1, iv2);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
           assertEquals("return value", "4CC9F77D7E734ABFBEB30104FDAFA3F7", actualReturn.getString(0));
       }
    }

    public void testDiversifyDES2_2()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "00000000010580";
        int op1 = HsmInterface.DES_ENCRYPT_CBC;
        int op2 = HsmInterface.DES_ENCRYPT_ECB;
        String divData1 = "12345678EDCBA987EDCBA98712345678";
        String divData2 = "1234567890123456EDCBA9876FEDCBA9";
        String iv1 = "1122334455667788";
        String iv2 = "0000000000000000";

        HsmResult actualReturn = hsmService.DiversifyDES2(sessionHandle, keyId, op1, op2, divData1, divData2, iv1, iv2);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
           assertEquals("return value", "A782AB07EE0014C868A5C21F5B7082BB", actualReturn.getString(0));
       }
    }

    public void testDiversifyDES2_3()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "00000000010580";
        int op1 = HsmInterface.DES_ENCRYPT_ECB;
        int op2 = HsmInterface.DES_ENCRYPT_CBC;
        String divData1 = "12345678EDCBA987EDCBA98712345678";
        String divData2 = "1234567890123456EDCBA9876FEDCBA9";
        String iv1 = "0000000000000000";
        String iv2 = "1122334455667788";

        HsmResult actualReturn = hsmService.DiversifyDES2(sessionHandle, keyId, op1, op2, divData1, divData2, iv1, iv2);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
           assertEquals("return value", "458ECC39923E15A7C19C2BA81E3380EA", actualReturn.getString(0));
       }
    }

    public void testDiversifyDES2_4()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "00000000010580";
        int op1 = HsmInterface.DES_DECRYPT_CBC;
        int op2 = HsmInterface.DES_DECRYPT_CBC;
        String divData1 = "12345678EDCBA987EDCBA98712345678";
        String divData2 = "1234567890123456EDCBA9876FEDCBA9";
        String iv1 = "1122334455667788";
        String iv2 = "1122334455667788";

        HsmResult actualReturn = hsmService.DiversifyDES2(sessionHandle, keyId, op1, op2, divData1, divData2, iv1, iv2);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
           assertEquals("return value", "6075017D8448370CED78F64EFBA07113", actualReturn.getString(0));
       }
    }


    public void testExportDES_0_byKEK_0()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_0_byKEK_0(sessionHandle, keyId, kekId, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "15F7E23E7739FB3115F7E23E7739FB31", actualReturn.getString(0));
            assertEquals("return value", "DD600F71D757FBAC", actualReturn.getString(1));
        }


    }

    public void testExportDES_0_byKEK_1()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String kekDivData = "33333333333333333333333333333333";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_0_byKEK_1(sessionHandle, keyId, kekId, kekDivData, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "DA3392A8E929F147DA3392A8E929F147", actualReturn.getString(0));
            assertEquals("return value", "DD600F71D757FBAC", actualReturn.getString(1));
        }
    }

    public void testExportDES_0_byKEK_2()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String kekDivData1 = "22222222222222222222222222222222";
        String kekDivData2 = "33333333333333333333333333333333";
        String kcvData = "5555555555555555";
        HsmResult actualReturn = hsmService.ExportDES_0_byKEK_2(sessionHandle, keyId, kekId, kekDivData1, kekDivData2, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "F325C9247D8E1A6EF325C9247D8E1A6E", actualReturn.getString(0));
            assertEquals("return value", "DD600F71D757FBAC", actualReturn.getString(1));
        }
    }

    public void testExportDES_1_byKEK_0()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String divData = "33333333333333333333333333333333";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_1_byKEK_0(sessionHandle, keyId, kekId, divData, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "3AAC08444E71DAAD3AAC08444E71DAAD", actualReturn.getString(0));
            assertEquals("return value", "567FB89AB954F6A2", actualReturn.getString(1));
        }
    }

    public void testExportDES_1_byKEK_1()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String divData = "33333333333333333333333333333333";
        String kekDivData = "22222222222222222222222222222222";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_1_byKEK_1(sessionHandle, keyId, kekId, divData, kekDivData, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "312079AF3E7C0A4B312079AF3E7C0A4B", actualReturn.getString(0));
            assertEquals("return value", "567FB89AB954F6A2", actualReturn.getString(1));
        }
    }

    public void testExportDES_1_byKEK_2()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String divData = "33333333333333333333333333333333";
        String kekDivData1 = "22222222222222222222222222222222";
        String kekDivData2 = "44444444444444444444444444444444";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_1_byKEK_2(sessionHandle, keyId, kekId, divData, kekDivData1, kekDivData2, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "070F11B704E031A2070F11B704E031A2", actualReturn.getString(0));
            assertEquals("return value", "567FB89AB954F6A2", actualReturn.getString(1));
        }
    }

    public void testExportDES_2_byKEK_0()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String divData1 = "22222222222222222222222222222222";
        String divData2 = "33333333333333333333333333333333";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_2_byKEK_0(sessionHandle, keyId, kekId, divData1, divData2, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "B250E33069E83D0DB250E33069E83D0D", actualReturn.getString(0));
            assertEquals("return value", "9486BD6D852BE4FD", actualReturn.getString(1));
        }
    }

    public void testExportDES_2_byKEK_1()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String divData1 = "22222222222222222222222222222222";
        String divData2 = "33333333333333333333333333333333";
        String kekDivData = "44444444444444444444444444444444";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_2_byKEK_1(sessionHandle, keyId, kekId, divData1, divData2, kekDivData, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "F9EC2DD14D2262A4F9EC2DD14D2262A4", actualReturn.getString(0));
            assertEquals("return value", "9486BD6D852BE4FD", actualReturn.getString(1));
        }
    }

    public void testExportDES_2_byKEK_2()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";
        String kekId = "ABC2";
        String divData1 = "22222222222222222222222222222222";
        String divData2 = "33333333333333333333333333333333";
        String kekDivData1 = "44444444444444444444444444444444";
        String kekDivData2 = "55555555555555555555555555555555";
        String kcvData = "5555555555555555";

        HsmResult actualReturn = hsmService.ExportDES_2_byKEK_2(sessionHandle, keyId, kekId, divData1, divData2, kekDivData1, kekDivData2, kcvData);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK)
        {
            assertEquals("return value", "494DFB33E544342F494DFB33E544342F", actualReturn.getString(0));
            assertEquals("return value", "9486BD6D852BE4FD", actualReturn.getString(1));
        }
    }

    public void testGenerateMAC_1()
    {
        int sessionHandle = HsmInterface.HSM_A;
        int op = HsmInterface.DES_ENCRYPT_CBC;
        String keyId = "ABC";
        String divData = "55555555555555555555555555555555";
        int size = 16;
        String data = "33333333333333333333333333333333";
        String iv = "4444444444444444";

        HsmResult actualReturn = hsmService.GenerateMAC_1(sessionHandle, op, keyId, divData, size, data, iv);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
           assertEquals("return value", "F37A7E16AC65A93C", actualReturn.getString(0));
        }
    }

    public void testIsKeyExisting()
    {
        int sessionHandle = HsmInterface.HSM_A;
        String keyId = "ABC";

        HsmResult actualReturn = hsmService.IsKeyExisting(sessionHandle, keyId);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());

        keyId = "DEF";

        actualReturn = hsmService.IsKeyExisting(sessionHandle, keyId);
        assertEquals("return value", HsmInterface.KEY_NON_EXIST, actualReturn.getValue());


    }

    public void testRandomNumber()
    {
        int sessionHandle = HsmInterface.HSM_A;
        int size = 4;
        String random1 = "";
        String random2 = "";

        HsmResult actualReturn = hsmService.RandomNumber(sessionHandle, size);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
           random1 = actualReturn.getString(0);
           assertEquals("return value", 4, random1.length()/2);
        }

        actualReturn = hsmService.RandomNumber(sessionHandle, size);
        assertEquals("return value", HsmInterface.HS_OK, actualReturn.getValue());
        if (actualReturn.getValue() == HsmInterface.HS_OK) {
           random2 = actualReturn.getString(0);
           assertEquals("return value", 4, random2.length()/2);
        }

        assertFalse("return value",random1.equals(random2));
    }


}
