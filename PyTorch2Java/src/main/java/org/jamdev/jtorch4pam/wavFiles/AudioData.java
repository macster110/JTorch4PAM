package org.jamdev.jtorch4pam.wavFiles;

import java.util.Arrays;

/**
 * Holds .wav file data. 
 * 
 * @author Jamie Macaulay
 *
 */
public class AudioData {

	/**
	 * Interpolator for wav files. 
	 */
	public WavInterpolator wavInterpolator; 

	/**
	 * Pre-emphasis filter. 
	 */
	public PreEmphasisFilter preEmphasisFilter; 

	/**
	 * Samples in amplitude units u. 
	 */
	public int[] samples;

	/**
	 * The sample rate in samples per second
	 */
	public float sampleRate;

	/**
	 * The bit rate of samples
	 */
	public double bitRate = 16; 

	/**
	 * Create an AudioData object. 
	 * @param samples - the samples in amplitude units u. 
	 * @param sampleRate - the sample rate in samples per second. 
	 */
	public AudioData(int[] samples, float sampleRate){
		this.samples=samples; 
		this.sampleRate = sampleRate;
		preEmphasisFilter = new PreEmphasisFilter(); 
		wavInterpolator= new WavInterpolator(); 
	}
	
	/**
	 * Create an AudioData object.
	 * @param samples - the samples in amplitude units u. 
	 * @param sampleRate - the sample rate in samples per second. 
	 */
	public AudioData(double[] wavArray, float sampleRate){
		int[] samples = new int[wavArray.length]; 
		for (int i=0; i<wavArray.length; i++) {
			wavArray[i] = samples[i]/bitRate; 
		}
		this.sampleRate = sampleRate;
		preEmphasisFilter = new PreEmphasisFilter(); 
		wavInterpolator= new WavInterpolator(); 
	}

	/**
	 * Get the sample rate in samples per second. 
	 * @return the sample rate in samples per second. 
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * Get the sample amplitudes in raw bits e.g. +/- 2^16 /2 for 16 bit files. 
	 * @return the sample amplitudes
	 */
	public int[] getSampleAmplitudes() {
		return samples;
	}

	/**
	 * Get the length in seconds. 
	 * @return the length of the file in seconds
	 */
	public double getLengthInSeconds() {
		return samples.length / (double) sampleRate;
	}

	/**
	 * Get the sample amplitudes as a double scaled between -1 and 1. 
	 * @return the scaled amplitude samples. 
	 */
	public double[] getScaledSampleAmpliudes() {
		double bitSize = Math.pow(2, bitRate); 

		double[] wavArray = new double[samples.length]; 
		for (int i=0; i<wavArray.length; i++) {
			wavArray[i] = samples[i]/bitSize; 
		}
		return wavArray; 
	}

	/**
	 * Trim the audio data between two sample indexes. 
	 * @param sampleStart - the sample start. 
	 * @param samplEnd - the sample end. 
	 * @return AudioData object with trimmed data. 
	 */
	public AudioData trim(int sampleStart, int samplEnd) {
		return new AudioData(Arrays.copyOfRange(samples, sampleStart, samplEnd), sampleRate); 
	}



	/**
	 * Interpolate the audio data (e..g up sample or down sample)
	 * @param interpSr - the new sample rate. 
	 * @return AudioData object containing interpolated data and sample rate. 
	 */
	public AudioData interpolate(float interpSr) {
		double[] wavArray = getScaledSampleAmpliudes(); 
		
		double[] intperarr = wavInterpolator.interpolate(wavArray, this.sampleRate, interpSr);

		int[] samplesDecimated = new int[intperarr.length]; 
		
		double bitSize = Math.pow(2, bitRate); 
		for (int i=0; i<intperarr.length; i++) {
			samplesDecimated[i]=(int) (bitSize*intperarr[i]);
		}

		return new AudioData(samplesDecimated, interpSr); 
	}

	/**
	 * Pre-emphasise the audio data so that lower frequencies are attenuated
	 * @param factor - the pre-emphasis factor
	 * @return AudioData object with pre emphasised data. 
	 */
	public AudioData preEmphasis(double factor) {
		return new AudioData(preEmphasisFilter.preEmphasis(this.samples, factor) , sampleRate); 
	}


}
