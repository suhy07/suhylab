package suhylab.suhy.video;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EglBase mRootEglBase;
    private SurfaceViewRenderer mLocalSurfaceView;
    private SurfaceViewRenderer mRemoteSurfaceView;
    private PeerConnectionFactory mPeerConnectionFactory;
    private VideoCapturer mVideoCapturer;
    private SurfaceTextureHelper mSurfaceTextureHelper;
    private VideoTrack mVideoTrack;
    private AudioTrack mAudioTrack;
    public static final String VIDEO_TRACK_ID = "1";//"ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "2";//"ARDAMSa0";
    private static final int VIDEO_RESOLUTION_WIDTH = 1280;
    private static final int VIDEO_RESOLUTION_HEIGHT = 720;
    private static final int VIDEO_FPS = 30;
    private PeerConnection mPeerConnection;
    private AudioManager audioManager;

    private static String TAG = "aaaaaaaaaaaaaaaaaaaa";
    private EditText et_offer;
    private Button bt_offer;
    private EditText et_answer;
    private Button bt_answer;
    private TextView tv_candicate1;
    private TextView tv_candicate2;
    private TextView tv_candicate3;
    private TextView tv_candicate4;
    private TextView tv_candicate5;
    private TextView tv_candicate6;
    private int count = 0;
    private Button bt_candidate1;
    private Button bt_candidate2;
    private Button bt_candidate3;
    private Button bt_candidate4;
    private Button bt_candidate5;
    private Button bt_candidate6;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                )
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "即将申请的权限是程序必须依赖的权限", "我已明白");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            // TODO: 2020/10/14 权限申请成功
                        } else {
                            Toast.makeText(MainActivity.this, "您拒绝了如下权限：" + deniedList, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        init();
    }
    /**
     * 开始webRtc的初始化
     */
    private void init() {
        //初始化视频必要对象
        mRootEglBase = EglBase.create();
        //两个视频画面
        mLocalSurfaceView = findViewById(R.id.LocalSurfaceView);
        mRemoteSurfaceView = findViewById(R.id.RemoteSurfaceView);
        et_offer = findViewById(R.id.et_offer);
        bt_offer = findViewById(R.id.bt_offer);
        et_answer = findViewById(R.id.et_answer);
        bt_answer = findViewById(R.id.bt_answer);
        tv_candicate1 = findViewById(R.id.tv_candicate1);
        tv_candicate2 = findViewById(R.id.tv_candicate2);
        tv_candicate3 = findViewById(R.id.tv_candicate3);
        tv_candicate4 = findViewById(R.id.tv_candicate4);
        tv_candicate5 = findViewById(R.id.tv_candicate5);
        tv_candicate6 = findViewById(R.id.tv_candicate6);
        bt_candidate1 = findViewById(R.id.bt_candidate1);
        bt_candidate2 = findViewById(R.id.bt_candidate2);
        bt_candidate3 = findViewById(R.id.bt_candidate3);
        bt_candidate4 = findViewById(R.id.bt_candidate4);
        bt_candidate5 = findViewById(R.id.bt_candidate5);
        bt_candidate6 = findViewById(R.id.bt_candidate6);
        button=findViewById(R.id.button);
        //初始化本地视频界面
        mLocalSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
        mLocalSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mLocalSurfaceView.setMirror(true);
        mLocalSurfaceView.setEnableHardwareScaler(false /* enabled */);
        //初始化对方视频界面
        mRemoteSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
        mRemoteSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mRemoteSurfaceView.setMirror(true);
        mRemoteSurfaceView.setEnableHardwareScaler(false /* enabled */);
        mRemoteSurfaceView.setZOrderMediaOverlay(true);
        //创建PeerConnectionFactory对象
        mPeerConnectionFactory = createPeerConnectionFactory(this);
        //日志输出设置
        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);
        //VideoCapturer对象初始化
        mVideoCapturer = createVideoCapturer();
        //相关视频源初始化，必备
        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        mVideoCapturer.initialize(mSurfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        //视频轨初始化，并设置到本地画布上
        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        mVideoTrack.setEnabled(true);
        mVideoTrack.addSink(mLocalSurfaceView);
        //音频轨初始化
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        mAudioTrack.setEnabled(true);
        //初始化音频控制器
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setSpeakerphoneOn(true);
        bt_offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                //接收到对放信令后的一个设置
                try {
                    String description = et_offer.getText().toString();
                    et_offer.setText("");
                    mPeerConnection.setRemoteDescription(
                            new SimpleSdpObserver() {
                                @Override
                                public void onSetSuccess() {
                                    Log.i(TAG, "set remote Description ok");
                                    doAnswerCall();
                                }

                                @Override
                                public void onSetFailure(String msg) {
                                    Log.i(TAG, "set remote Description unok");
                                }
                            },
                            new SessionDescription(
                                    SessionDescription.Type.OFFER,
                                    description));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bt_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String answer = et_answer.getText().toString();
                et_answer.setText("");
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                mPeerConnection.setRemoteDescription(
                        new SimpleSdpObserver() {
                            @Override
                            public void onSetSuccess() {
                            }

                            @Override
                            public void onSetFailure(String msg) {
                            }
                        },
                        new SessionDescription(
                                SessionDescription.Type.ANSWER,
                                answer));
            }
        });
        bt_candidate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                String json = tv_candicate1.getText().toString().trim();
                tv_candicate1.setText("");
                CandidateBean candidateBean = new Gson().fromJson(json, CandidateBean.class);
                IceCandidate remoteIceCandidate =
                        new IceCandidate(candidateBean.getId(),
                                candidateBean.getLabel(),
                                candidateBean.getCandidate());
                mPeerConnection.addIceCandidate(remoteIceCandidate);
            }
        });
        bt_candidate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                String json = tv_candicate2.getText().toString().trim();
                tv_candicate2.setText("");
                CandidateBean candidateBean = new Gson().fromJson(json, CandidateBean.class);
                IceCandidate remoteIceCandidate =
                        new IceCandidate(candidateBean.getId(),
                                candidateBean.getLabel(),
                                candidateBean.getCandidate());
                mPeerConnection.addIceCandidate(remoteIceCandidate);
            }
        });
        bt_candidate3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                String json = tv_candicate3.getText().toString().trim();
                tv_candicate3.setText("");
                CandidateBean candidateBean = new Gson().fromJson(json, CandidateBean.class);
                IceCandidate remoteIceCandidate =
                        new IceCandidate(candidateBean.getId(),
                                candidateBean.getLabel(),
                                candidateBean.getCandidate());
                mPeerConnection.addIceCandidate(remoteIceCandidate);
            }
        });
        bt_candidate4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                String json = tv_candicate4.getText().toString().trim();
                tv_candicate4.setText("");
                CandidateBean candidateBean = new Gson().fromJson(json, CandidateBean.class);
                IceCandidate remoteIceCandidate =
                        new IceCandidate(candidateBean.getId(),
                                candidateBean.getLabel(),
                                candidateBean.getCandidate());
                mPeerConnection.addIceCandidate(remoteIceCandidate);
            }
        });
        bt_candidate5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                String json = tv_candicate5.getText().toString().trim();
                tv_candicate5.setText("");
                CandidateBean candidateBean = new Gson().fromJson(json, CandidateBean.class);
                IceCandidate remoteIceCandidate =
                        new IceCandidate(candidateBean.getId(),
                                candidateBean.getLabel(),
                                candidateBean.getCandidate());
                mPeerConnection.addIceCandidate(remoteIceCandidate);
            }
        });
        bt_candidate6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPeerConnection == null) {
                    mPeerConnection = createPeerConnection();
                }
                String json = tv_candicate6.getText().toString().trim();
                tv_candicate6.setText("");
                CandidateBean candidateBean = new Gson().fromJson(json, CandidateBean.class);
                IceCandidate remoteIceCandidate =
                        new IceCandidate(candidateBean.getId(),
                                candidateBean.getLabel(),
                                candidateBean.getCandidate());
                mPeerConnection.addIceCandidate(remoteIceCandidate);
            }
        });
        mVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, VIDEO_FPS);
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }
        button.setOnClickListener(v->doStartCall());
    }

    /**
     * 回复
     */
    public void doAnswerCall() {
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        mPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(final SessionDescription sessionDescription) {
                mPeerConnection.setLocalDescription(new SimpleSdpObserver() {
                                                        @Override
                                                        public void onSetFailure(String msg) {
                                                        }

                                                        @Override
                                                        public void onSetSuccess() {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    et_answer.setText(sessionDescription.description);
                                                                }
                                                            });
                                                        }
                                                    },
                        sessionDescription);
            }

            @Override
            public void onCreateFailure(String msg) {
            }
        }, sdpMediaConstraints);
    }

    /**
     * 创建PeerConnectionFactory对象
     *
     * @param context
     * @return
     */
    public PeerConnectionFactory createPeerConnectionFactory(Context context) {
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        encoderFactory = new DefaultVideoEncoderFactory(
                mRootEglBase.getEglBaseContext(),
                false /* enableIntelVp8Encoder */,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions());
        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory);
        builder.setOptions(null);
        return builder.createPeerConnectionFactory();
    }

    /**
     * 创建VideoCapturer对象
     *
     * @return
     */
    private VideoCapturer createVideoCapturer() {
        if (Camera2Enumerator.isSupported(this)) {
            return createCameraCapturer(new Camera2Enumerator(this));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    /**
     * 根据不同摄像头初始化VideoCapturer对象
     *
     * @param enumerator
     * @return
     */
    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: 2021/1/6 缺一个挂电话的动作
        mLocalSurfaceView.release();
        mRemoteSurfaceView.release();
        mVideoCapturer.dispose();
        mSurfaceTextureHelper.dispose();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        mPeerConnectionFactory.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, VIDEO_FPS);
//        //开始创建PeerConnection
//        //默认开启本地视频后就创建这个PeerConnection
//        if (mPeerConnection == null) {
//            mPeerConnection = createPeerConnection();
//        }
//        doStartCall();
    }

    /**
     * 创建PeerConnection
     *
     * @return
     */
    public PeerConnection createPeerConnection() {
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
//        PeerConnection.IceServer ice_server =
//                    PeerConnection.IceServer.builder("turn:xxxx:3478")
//                                            .setPassword("xxx")
//                                            .setUsername("xxx")
//                                            .createIceServer();
        PeerConnection.IceServer ice_server = PeerConnection
                .IceServer
                .builder("stun:stun.l.google.com:19302")
                .createIceServer();
        iceServers.add(ice_server);
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        //TCP候选策略控制开关
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        //只使用TCP中转模式，不使用p2p直连
        //rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.RELAY;
        //rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        //rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        //rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = true;
        //rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        PeerConnection connection =
                mPeerConnectionFactory.createPeerConnection(rtcConfig,
                        mPeerConnectionObserver);
        if (connection == null) {
            return null;
        }

        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        connection.addTrack(mVideoTrack, mediaStreamLabels);
        connection.addTrack(mAudioTrack, mediaStreamLabels);
        connection.setAudioPlayout(true);
        return connection;
    }

    /**
     * 创建PeerConnection的回调
     */
    private PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }

        /**
         * 获取到当前设备的Candidate
         * @param iceCandidate
         */
        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            JSONObject message = new JSONObject();
            try {
                message.put("type", "candidate");
                message.put("label", iceCandidate.sdpMLineIndex);
                message.put("id", iceCandidate.sdpMid);
                message.put("candidate", iceCandidate.sdp);
                Log.i(TAG, "candidate:" + message.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        count++;
                        if (count == 1) {
                            tv_candicate1.setText(message.toString());
                        } else if (count == 2) {
                            tv_candicate2.setText(message.toString());
                        } else if (count == 3) {
                            tv_candicate3.setText(message.toString());
                        } else if (count == 4) {
                            tv_candicate4.setText(message.toString());
                        } else if (count == 5) {
                            tv_candicate5.setText(message.toString());
                        } else if (count == 6) {
                            tv_candicate6.setText(message.toString());
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            if (iceCandidates != null) {
                for (int i = 0; i < iceCandidates.length; i++) {
                }
                mPeerConnection.removeIceCandidates(iceCandidates);
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
//            VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
//            remoteVideoTrack.setEnabled(true);
//            remoteVideoTrack.addSink(mRemoteSurfaceView);

//            VideoTrack videoTrack = mediaStream.videoTracks.get(0);
//            videoTrack.setEnabled(true);
//            videoTrack.addSink(mRemoteSurfaceView);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
        }

        @Override
        public void onRenegotiationNeeded() {
        }

        /**
         * 接收到对方的视频
         * @param rtpReceiver
         * @param mediaStreams
         */
        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            MediaStreamTrack track = rtpReceiver.track();
            if (track instanceof VideoTrack) {
                Log.i(TAG, "有视频");
                VideoTrack remoteVideoTrack = (VideoTrack) track;
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addSink(mRemoteSurfaceView);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
//        try {
//            mVideoCapturer.stopCapture();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 开始呼叫
     */
    public void doStartCall() {
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        mPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                mPeerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override
                    public void onSetSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                et_offer.setText(sessionDescription.description);
                            }
                        });
                    }

                    @Override
                    public void onSetFailure(String msg) {
                    }
                }, sessionDescription);
            }
        }, mediaConstraints);
    }

    public static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String msg) {
        }

        @Override
        public void onSetFailure(String msg) {
        }
    }

    private void setSpeakerphoneOn(boolean on) {
        if (on) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
            //把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }
}