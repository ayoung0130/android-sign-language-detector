package com.example.sign_language_detector.ui.camera

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.sign_language_detector.R
import com.example.sign_language_detector.databinding.FragmentCameraBinding
import com.example.sign_language_detector.repository.HandLandmarkerHelper
import com.example.sign_language_detector.repository.PoseLandmarkerHelper
import com.example.sign_language_detector.ui.splash.SplashFragment
import com.example.sign_language_detector.usecase.CameraUseCase
import com.example.sign_language_detector.usecase.DetectUseCase
import com.example.sign_language_detector.util.LandmarkProcessor
import com.example.sign_language_detector.util.ModelPredictProcessor
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.min

class CameraFragment : Fragment(), HandLandmarkerHelper.LandmarkerListener,
    PoseLandmarkerHelper.LandmarkerListener {

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var viewModel: CameraViewModel
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper

    private var imageHandAnalyzer: ImageAnalysis? = null
    private var imagePoseAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private var handResultBundle: HandLandmarkerHelper.ResultBundle? = null
    private var poseResultBundle: PoseLandmarkerHelper.ResultBundle? = null

    private val poseLandmarkIndices = listOf(
        0, 2, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24
    )

    private val storedLandmarkData = mutableListOf<FloatArray>()

    /** 차단된 ML 작업은 이 실행기를 사용하여 수행 */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // 모든 권한이 여전히 존재하는지 확인, 사용자가 앱이 일시 중지된 동안 이를 제거할 수 있음
        if (!SplashFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_splash_to_home)
        }

        // 사용자가 전면으로 돌아올 때 HandLandmarkerHelper를 다시 시작
        backgroundExecutor.execute {
            if (handLandmarkerHelper.isClose()) {
                handLandmarkerHelper.setupHandLandmarker()
            }
            if (poseLandmarkerHelper.isClose()) {
                poseLandmarkerHelper.setupPoseLandmarker()
            }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // 백그라운드 실행기 종료
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 백그라운드 실행기 초기화
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // HandLandmarkerHelper와 PoseLandmarkerHelper를 직접 초기화
        handLandmarkerHelper = HandLandmarkerHelper(
            context = requireContext(),
            runningMode = RunningMode.LIVE_STREAM,
            minHandDetectionConfidence = 0.5f,
            minHandTrackingConfidence = 0.5f,
            minHandPresenceConfidence = 0.5f,
            maxNumHands = 2,
            currentDelegate = 0,
            handLandmarkerHelperListener = this@CameraFragment
        )
        poseLandmarkerHelper = PoseLandmarkerHelper(
            context = requireContext(),
            runningMode = RunningMode.LIVE_STREAM,
            minPoseDetectionConfidence = 0.5f,
            minPoseTrackingConfidence = 0.5f,
            minPosePresenceConfidence = 0.5f,
            currentDelegate = 0,
            poseLandmarkerHelperListener = this@CameraFragment
        )

        // ViewModel 초기화
        val detectUseCase = DetectUseCase(
            handLandmarkerHelper = handLandmarkerHelper,
            poseLandmarkerHelper = poseLandmarkerHelper,
            executor = backgroundExecutor
        )
        val landmarkProcessor = LandmarkProcessor()
        val modelPredictProcessor = ModelPredictProcessor(requireContext())
        val factory =
            CameraViewModelFactory(detectUseCase, landmarkProcessor, modelPredictProcessor)
        viewModel = ViewModelProvider(this, factory)[CameraViewModel::class.java]

        fragmentCameraBinding.viewModel = viewModel
        fragmentCameraBinding.lifecycleOwner = viewLifecycleOwner

        Log.d("tag", "ViewModel initialized: $viewModel")

        // 뷰가 제대로 배치될 때까지 대기
        fragmentCameraBinding.viewFinder.post {
            // 카메라와 그 사용 사례를 설정
            setUpCamera()
        }

        // 버튼 클릭에 따른 네비게이션 설정
        viewModel.navigateToHome = {
            findNavController().navigateUp()
        }

        viewModel.navigateToQuestions = {
            findNavController().navigate(R.id.action_camera_to_questions)
        }

        viewModel.navigateToSignLanguage = {
            findNavController().navigate(R.id.action_camera_to_sign_language)
        }

        viewModel.navigateBack = {
            findNavController().navigateUp()
        }
    }

    // CameraX 초기화 및 카메라 사용 사례 준비
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // 카메라 사용 사례 빌드 및 바인딩
                val cameraUseCase = CameraUseCase(
                    this,
                    cameraFacing,
                    fragmentCameraBinding.viewFinder,
                    viewModel,
                    backgroundExecutor
                )
                cameraUseCase.bindCameraUseCases(cameraProvider!!)
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageHandAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
        imagePoseAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // 손이 감지된 후 UI 업데이트. 오리지널 이미지 높이/너비를 추출하고 캔버스를 통해 랜드마크를 올바르게 배치
    override fun onHandResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null && resultBundle.results.isNotEmpty()) {
                // 필요한 정보를 OverlayView에 전달하여 캔버스에 그림
                fragmentCameraBinding.overlay.setHandResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,  //640
                    resultBundle.inputImageWidth,   //480
                    RunningMode.LIVE_STREAM
                )
                // 다시 그리기 강제
                fragmentCameraBinding.overlay.invalidate()

                handResultBundle = resultBundle
                processCombinedResults()
            }
        }
    }

    // 포즈가 감지된 후 UI 업데이트
    override fun onPoseResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null && resultBundle.results.isNotEmpty()) {
                fragmentCameraBinding.overlay.setPoseResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )
                fragmentCameraBinding.overlay.invalidate()

                poseResultBundle = resultBundle
            }
        }
    }

    private fun processCombinedResults() {

        // 손과 포즈 랜드마크 데이터가 존재하면 (손/포즈 동시 검출시)
        if (handResultBundle?.results?.first()?.landmarks()?.isNotEmpty() == true &&
            poseResultBundle?.results?.first()?.landmarks()?.isNotEmpty() == true
        ) {

            val jointLeftHands = Array(21) { FloatArray(3) }
            val jointRightHands = Array(21) { FloatArray(3) }
            val jointPose = Array(21) { FloatArray(3) }

            handResultBundle!!.results.forEach { result ->
                result.landmarks().forEachIndexed { i, hand ->
                    // 손의 핸디드니스에 따라 적절한 배열에 값을 저장
                    if (result.handedness()[i].first().categoryName() == "Left") {
                        hand.forEachIndexed { j, lm ->
                            jointRightHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z())
                        }
                    } else {
                        hand.forEachIndexed { j, lm ->
                            jointLeftHands[j] = floatArrayOf(lm.x(), lm.y(), lm.z())
                        }
                    }
                }
            }

            poseResultBundle!!.results.forEach { result ->
                result.landmarks().forEachIndexed { _, pose ->
                    pose.forEachIndexed { j, lm ->
                        if (j in poseLandmarkIndices) {
                            jointPose[poseLandmarkIndices.indexOf(j)] = floatArrayOf(
                                lm.x(), lm.y(), lm.z()
                            )
                        }
                    }
                }
            }

            val leftHandAngle = viewModel.calculateHandAngle(jointLeftHands)
            val rightHandAngle = viewModel.calculateHandAngle(jointRightHands)
            val poseAngle = viewModel.calculatePoseAngle(jointPose)

            // 모든 랜드마크와 각도 데이터를 하나의 배열로 결합
            val jointData = (jointLeftHands.flatMap { it.toList() } +
                    jointRightHands.flatMap { it.toList() } +
                    jointPose.flatMap { it.toList() }).toFloatArray()

            val data =
                (jointData + leftHandAngle + rightHandAngle + poseAngle)

            Log.d("tag", "data size: ${data.size}")

            storedLandmarkData.add(data)
            Log.d("tag", "storedData size: ${storedLandmarkData.size}")

        } else if (storedLandmarkData.isNotEmpty()) {
            if (storedLandmarkData.size > 15) {
                Log.d("tag", "손 내려감! 예측 수행")
                viewModel.updatePredictedWord(storedLandmarkData, requireContext())
                storedLandmarkData.clear()
            } else {
                Toast.makeText(context, "동작을 더 길게 수행해주세요", Toast.LENGTH_SHORT).show()
                storedLandmarkData.clear()
            }
        }
    }

    override fun onHandError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPoseError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
}
