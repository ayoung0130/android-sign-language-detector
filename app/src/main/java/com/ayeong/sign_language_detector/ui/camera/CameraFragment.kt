package com.ayeong.sign_language_detector.ui.camera

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
import com.ayeong.sign_language_detector.R
import com.ayeong.sign_language_detector.databinding.FragmentCameraBinding
import com.ayeong.sign_language_detector.repository.HandLandmarkerHelper
import com.ayeong.sign_language_detector.repository.PoseLandmarkerHelper
import com.ayeong.sign_language_detector.ui.splash.SplashFragment
import com.ayeong.sign_language_detector.usecase.CameraUseCase
import com.ayeong.sign_language_detector.usecase.DetectUseCase
import com.ayeong.sign_language_detector.util.LandmarkProcessor
import com.ayeong.sign_language_detector.util.ModelPredictProcessor
import com.ayeong.sign_language_detector.util.ProcessTts
import com.ayeong.sign_language_detector.util.WordsToSentence
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), HandLandmarkerHelper.LandmarkerListener,
    PoseLandmarkerHelper.LandmarkerListener {

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

    private lateinit var viewModel: CameraViewModel
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT

    private var handResultBundle: HandLandmarkerHelper.ResultBundle? = null
    private var poseResultBundle: PoseLandmarkerHelper.ResultBundle? = null

    private val storedLandmarkData = mutableListOf<FloatArray>()
    private var handMissingFrameCount = 0  // 손이 인식되지 않은 프레임 수 카운터
    private val maxHandMissingFrames = 5   // 손이 사라졌다고 판단할 프레임 수 임계값

    /** 차단된 ML 작업은 이 실행기를 사용하여 수행 */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // 백그라운드 실행기 종료
        backgroundExecutor.shutdown()
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
        val wordsToSentence = WordsToSentence()
        val processTts = ProcessTts(requireContext())
        val factory =
            CameraViewModelFactory(
                detectUseCase,
                landmarkProcessor,
                modelPredictProcessor,
                wordsToSentence,
                processTts
            )
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
        with(viewModel) {
            navigateToHome = {
                findNavController().navigateUp()
            }
            navigateToQuestions = {
                findNavController().navigate(R.id.action_camera_to_questions)
            }
            navigateToSignLanguage = {
                findNavController().navigate(R.id.action_camera_to_sign_language)
            }
            navigateBack = {
                findNavController().navigateUp()
            }
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

    // 손이 감지된 후 UI 업데이트. 오리지널 이미지 높이/너비를 추출하고 캔버스를 통해 랜드마크를 올바르게 배치
    override fun onHandResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null && resultBundle.results.isNotEmpty()) {
                // 필요한 정보를 OverlayView에 전달하여 캔버스에 그림
                fragmentCameraBinding.overlay.setHandResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )
                // 다시 그리기 강제
                fragmentCameraBinding.overlay.invalidate()

                handResultBundle = resultBundle
                processCombinedResults()
            }
            if (resultBundle.results.first().landmarks().isEmpty()) {
                // 손이 인식되지 않은 경우: 카운터 증가
                handMissingFrameCount++

                // 카운터가 임계값을 넘으면 데이터를 삭제
                if (handMissingFrameCount >= maxHandMissingFrames) {
                    storedLandmarkData.clear()
                }
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
            viewModel.onBothDetected(true)

            val processedLandmarks =
                viewModel.processLandmarks(handResultBundle!!, poseResultBundle!!)

            storedLandmarkData.add(processedLandmarks)
            Log.d("tag", "storedData size: ${storedLandmarkData.size}")

        } else if (storedLandmarkData.isNotEmpty()) {
            if (storedLandmarkData.size > 15) {
                Log.d("tag", "손 내려감! 예측 수행")

                val predictedWords = viewModel.modelPredict(storedLandmarkData)
                viewModel.processWords(predictedWords)

            } else if (storedLandmarkData.size > 5) {
                Toast.makeText(context, "동작을 더 길게 수행해주세요", Toast.LENGTH_SHORT).show()
            }

            // 데이터가 클리어될 때 카운터도 리셋
            viewModel.onBothDetected(false)
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
