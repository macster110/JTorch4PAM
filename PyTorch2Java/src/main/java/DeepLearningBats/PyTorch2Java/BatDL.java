package DeepLearningBats.PyTorch2Java;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import pytorch2Java.DLUtils;
import spectrogram.Spectrogram;
import wavFiles.WavFile;
import wavFiles.AudioData;
/**
 * Run a bat deep learning algorithm. 
 *
 */
public class BatDL {


	/*
	 * Load a wav file. 
	 */
	public static AudioData loadWavFile(String path) throws IOException, UnsupportedAudioFileException {
		// reads the first 44 bytes for header
		WavFile wavFile = new  WavFile(new File(path));
		AudioFormat format = wavFile.getAudioFileFormat().getFormat(); 

		int channels = format.getChannels(); 

		// load data
		AudioInputStream inputStream  = wavFile.getAudioInputStream(); 

		//first downsample
		//now downsample the data if need bed 
		byte[] data;

		data = new byte[inputStream.available()];
		inputStream.read(data);	  
		//		}

		if (channels==1) {
			//no need to do anything else. 

		}
		else {
			//extract single channel data 
			data = WavFile.getSingleChannelByte(format, data,  0); 
		}

		int[] samples = WavFile.getSampleAmplitudes(format, data);

		int sampleRate = (int) format.getSampleRate();

		return new AudioData(samples, sampleRate); 

	}
	
	/**
	 * Make a dummy spectrgram for testing.  
	 * @return a dummy spectrogram with random values. 
	 */
	private static float[][] makeDummySpectrogram(){
		
		int len = 256; 
		int len2 = 128; 
		
		float[][] specDummy = new float[len][len2]; 

		Random rand = new Random(); 
		for (int i=0; i<len; i++){
			for (int j=0; j<len2; j++) {
				specDummy[i][j] = 2F*(rand.nextFloat()-0.5F);
				
				if (specDummy[i][j]>1) {
					specDummy[i][j]=1F;
				}
				if (specDummy[i][j]<0) {
					specDummy[i][j]=0F;
				}
			}
		}
		
		return specDummy; 
	}

	public static void main( String[] args ) {
		
		//create the DL params. 
		DLParams dlParams = new DLParams();
		
		//Path to the wav file 
//		String wavFilePath = "/Users/au671271/Google Drive/Aarhus_research/PAMGuard_bats_2020/deep_learning/BAT/example_wav/SKOVSOE_20200817_011402.wav"; 

		//High...ish SNR bat click
		String wavFilePath = "/Users/au671271/Google Drive/Aarhus_research/PAMGuard_bats_2020/deep_learning/training_clips__Troldekær_deployment_3/DUB_20200623_000152_885.wav";
		
				
		//Path to the model
		String modelPath = "/Users/au671271/Google Drive/Aarhus_research/PAMGuard_bats_2020/deep_learning/BAT/BAT_4ms_256ft_8hop_128_NOISEAUG_40000_100000_-100_0_256000.pk";

		//wav file 
		try {
			AudioData soundData = loadWavFile(wavFilePath);
			soundData = soundData.interpolate(dlParams.sR).preEmphasis(dlParams.); 
			
			System.out.println( "Open wav file: No. samples:"+ soundData.samples.length + " sample rate: " + soundData.sampleRate);


			//make a spectrogram 
			Spectrogram spectrgram = new Spectrogram(soundData, dlParams.n_fft, dlParams.hop_length); 

			spectrgram.dBSpec().normalise(dlParams.min_level_dB, dlParams.ref_level_dB).clamp(dlParams.clampMin, dlParams.clampMax);

//			//now must flatten the spectrogram and create a tensor.
//			float[][]  specGram = makeDummySpectrogram(); 
			
			float[] specgramFlat = DLUtils.flattenDoubleArrayF(DLUtils.toFloatArray(spectrgram.getSpectrogramArray())); 
			int[] arrayShape = 	DLUtils.arrayShape(spectrgram.getSpectrogramArray());
			
			long[] arrayShaleL = new long[arrayShape.length]; 
			for (int i=0; i<arrayShaleL.length; i++) {
				arrayShaleL[i] = arrayShape[i]; 
				System.out.println(arrayShaleL[i]); 
			}
			
			long[] shape = {1L, 1L, arrayShaleL[0], arrayShaleL[1]}; 
			
//			DLUtils.printArray(specGram); 
			
			//create the tensor 
			Tensor data = Tensor.fromBlob(specgramFlat, shape); 

			//load the model. 
			Module mod = Module.load(modelPath);

			IValue result = mod.forward(IValue.from(data));

			Tensor output = result.toTensor();
			
		    System.out.println("Output shape: " + Arrays.toString(output.shape()));
		    System.out.println("Output data: " + Arrays.toString(output.getDataAsFloatArray()));
			
			//grab the results. 
		    double[] prob = new double[(int) output.shape()[0]]; 
		    for (int j=0; j<output.shape()[0]; j++) {
		    	//python code for this. 
//		    	prob = torch.nn.functional.softmax(out).numpy()[n, 1]
//	                    pred = int(prob >= ARGS.threshold)		    	
		    	//softmax function
		    	prob[j] = DLUtils.softmax(output.getDataAsFloatArray()[j], output.getDataAsFloatArray()); 
		    	
		    	System.out.println("The probability is: " + prob[j]); 
		    }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}



}
