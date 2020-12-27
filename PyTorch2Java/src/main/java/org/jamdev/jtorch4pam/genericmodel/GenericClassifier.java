package org.jamdev.jtorch4pam.genericmodel;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.jamdev.jtorch4pam.SoundSpot.SoundSpotClassifier;
import org.jamdev.jtorch4pam.SoundSpot.SoundSpotModel;
import org.jamdev.jtorch4pam.SoundSpot.SoundSpotParams;
import org.jamdev.jtorch4pam.transforms.DLTransform;
import org.jamdev.jtorch4pam.transforms.DLTransformsFactory;
import org.jamdev.jtorch4pam.transforms.FreqTransform;
import org.jamdev.jtorch4pam.transforms.WaveTransform;
import org.jamdev.jtorch4pam.utils.DLUtils;
import org.jamdev.jtorch4pam.wavFiles.AudioData;

/**
 * Create the generic classifier. 
 * 
 * @author Jamie Macaulay
 *
 */
public class GenericClassifier {
	
	
	/**
	 * The generic model. 
	 */
	private GenericModel genericModel;
	
	
	/**
	 * The generic model parameters. 
	 */
	private GenericModelParams genericModelParams;
	
	public GenericClassifier(String modelPath) {
		loadModel( modelPath);
	}
	
	/**
	 * Load a sound spot model. This loads the model into memory
	 * and extracts the metadata from the model creating a SoundSpotParams class. 
	 * @param modelPath - the path to the model. 
	 * @return true if the model was loaded successfully. 
	 */
	public boolean loadModel(String modelPath) {
		//first open the model and get the correct parameters. 
		try {
			genericModel = new GenericModel(modelPath);
			//create the DL parameters. 
			genericModelParams = new GenericModelParams();
			return true; 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false; 
		} 
	}



	/**
	 * Run the currently loaded model on a chunk of sound data. 
	 * @param rawWaveData - a raw chunk of sound data with peak levels between -1 and 1. 
	 * @return the probability of each class in the model. 
	 */
	public double[] runModel(double[] rawWaveData, float sR) {

		//wav file 
		try {			

			//open .wav files. 
			AudioData soundData = new AudioData(rawWaveData, sR); 

			//generate the transforms. 
			ArrayList<DLTransform> transforms =	DLTransformsFactory.makeDLTransforms(genericModelParams.dlTransforms); 


			((WaveTransform) transforms.get(0)).setWaveData(soundData); 

			DLTransform transform = transforms.get(0); 
			for (int i=0; i<transforms.size(); i++) {
				transform = transforms.get(i).transformData(transform); 
			}


			float[] output = null; 
			float[][] data;
			for (int i=0; i<10; i++) {
				//long time1 = System.currentTimeMillis();
				data = DLUtils.toFloatArray(((FreqTransform) transform).getSpecTransfrom().getTransformedData()); 
				output = genericModel.runModel(data); 
				//long time2 = System.currentTimeMillis();
				//System.out.println("Time to run model: " + (time2-time1) + " ms"); 
			}

			double[] prob = new double[output.length]; 
			for (int j=0; j<output.length; j++) {
				//python code for this. 
				//				    	prob = torch.nn.functional.softmax(out).numpy()[n, 1]
				//			                    pred = int(prob >= ARGS.threshold)		    	
				//softmax function
				prob[j] = DLUtils.softmax(output[j], output); 
				//System.out.println("The probability is: " + prob[j]); 
			}

			return prob; 

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	/**
	 * Test the classifier on a wave file. 
	 * @param args - the arguments. 
	 */
	public static void main(String[] args) {
		
		// let's test on some right whale data. 
		
		String wavFilePath = "/Users/au671271/Google Drive/Aarhus_research/PAMGuard_bats_2020/deep_learning/BAT/example_wav/call_393_2019_S4U05619MOL2-20180917-051012_2525_2534.wav";
		int[] samplesChunk = new int[] {0, 1274}; // the sample chunk to use. 
		String modelPath = "/Users/au671271/Google Drive/PAMGuard_dev/Deep_Learning/Right_whales_DG/model_lenet_dropout_input_conv_all.hdf5"; 
	
		//Open wav files. 
		try {
			
			AudioData soundData;
			soundData = DLUtils.loadWavFile(wavFilePath);
			soundData = soundData.trim(samplesChunk[0], samplesChunk[1]); 
			
			GenericClassifier genericClassifier = new GenericClassifier(modelPath); 

			double[] result = genericClassifier.runModel(soundData.getScaledSampleAmpliudes(), soundData.sampleRate); 
			
		    for (int j=0; j<result.length; j++) {
		    	System.out.println("The probability of class " + j + " is "  + result[j]); 
		    }


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
