package com.syd.leshan;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.core.util.SecurityUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static org.eclipse.leshan.client.object.Security.x509;

/**
 * jni学习
 * https://www.jianshu.com/p/87ce6f565d37
 */
public class Test {

    private static LeshanClient client;
    private static final String SERVER_URI = "coap://localhost:5683";

    public static void main(String[] args) {
        PrivateKey clientPrivateKey = null;
        X509Certificate clientCertificate = null;
        X509Certificate serverCertificate = null;
        try {
            clientPrivateKey = SecurityUtil.privateKey.readFromFile("/Users/liuchao/syd/build_game/Leshan/src/main/resources/clientKeyStore.jks");
            clientCertificate = SecurityUtil.certificate.readFromFile("/Users/liuchao/syd/build_game/Leshan/src/main/resources/serverCertificate.der");
            serverCertificate = SecurityUtil.certificate.readFromFile("/Users/liuchao/syd/build_game/Leshan/src/main/resources/serverPubKey.der");
        } catch (Exception e) {
            System.err.println("Unable to load X509 files : " + e.getMessage());
            e.printStackTrace();
        }

        String endpoint = "..."; // choose an endpoint name
        LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
        ObjectsInitializer initializer = new ObjectsInitializer();
        initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec(SERVER_URI, 12345));
        initializer.setInstancesForObject(LwM2mId.SERVER, new Server(12345, 5 * 60, BindingMode.U, false));
        initializer.setInstancesForObject(LwM2mId.DEVICE, new Device("Eclipse Leshan", "model12345", "12345", "U"));
        initializer.setInstancesForObject(7, new ConnectivityStatistics());
        if (clientPrivateKey != null && clientCertificate != null && serverCertificate != null) {
            try {
                initializer.setInstancesForObject(LwM2mId.SECURITY, x509(SERVER_URI, 12345, clientCertificate.getEncoded(),
                        clientPrivateKey.getEncoded(), serverCertificate.getEncoded()));
                initializer.setInstancesForObject(LwM2mId.SERVER, new Server(123, 10000, BindingMode.U, false));
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
        }

// add it to the client
        builder.setObjects(initializer.createAll());
        client = builder.build();
        client.start();
    }

    public static class ConnectivityStatistics extends BaseInstanceEnabler {

        @Override
        public ReadResponse read(ServerIdentity identity, int resourceid) {
            switch (resourceid) {
                case 0:
                    System.out.println("Test " + identity.toString());
                    return ReadResponse.success(resourceid, "getSmsTxCounter()");
            }
            return ReadResponse.notFound();
        }

        @Override
        public WriteResponse write(ServerIdentity identity, int resourceid, LwM2mResource value) {
            switch (resourceid) {
                case 15:
                    //setCollectionPeriod((Long) value.getValue());
                    System.out.println(identity.toString());
                    System.out.println(value.toString());
                    return WriteResponse.success();
            }
            return WriteResponse.notFound();
        }

        @Override
        public ExecuteResponse execute(ServerIdentity identity, int resourceid, String params) {
            switch (resourceid) {
                case 12:
                    client.start();
                    return ExecuteResponse.success();
            }
            return ExecuteResponse.notFound();
        }
    }
}
