package com.taiwanlife.teamwalk;

import android.app.Application;

import com.taiwanlife.teamwalk.util.CelebrusCSAUtil;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

import devliving.online.securedpreferencestore.DefaultRecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

/**
 * Author : Ryans
 * Date : 2022/3/14
 * Introduction :
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try{
            CelebrusCSAUtil.instrument(this);
        }catch (Exception e){}

        try {
            // init securedSP
            SecuredPreferenceStore.init(getApplicationContext(), new DefaultRecoveryHandler());
        }catch (Exception e){}
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (UnrecoverableEntryException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        }
    }
}
