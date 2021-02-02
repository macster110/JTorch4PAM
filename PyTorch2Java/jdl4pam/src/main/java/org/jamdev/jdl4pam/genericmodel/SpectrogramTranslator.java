package org.jamdev.jdl4pam.genericmodel;

import org.jamdev.jdl4pam.utils.DLUtils;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * The translator for the model. Ensure the input data is compatible for the model and the output  data 
 * is properly organised. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SpectrogramTranslator implements Translator<double[][], float[]> {    

	@Override
	public NDList processInput(TranslatorContext ctx, double[][] data) {
		//System.out.println("Hello: 1 " ); 
		NDManager manager = ctx.getNDManager();

		Shape shape = new Shape(1L, data.length, data[0].length, 1L); 
		
		System.out.println("NDArray shape: " + shape); 

		double[] specgramFlat = DLUtils.flattenDoubleArray(data); 

		NDArray array = manager.create(specgramFlat, shape); 
//		NDArray array = manager.create(data); 

		System.out.println("NDArray size: " + array.size()); 

		return new NDList (array);
	}

	@Override
	public float[]  processOutput(TranslatorContext ctx, NDList list) {
		System.out.println("Hello: 2 " + list); 

		NDArray temp_arr = list.get(0);

		Number[] number = temp_arr.toArray(); 

		float[] results = new float[number.length]; 
		for (int i=0; i<number.length; i++) {
			results[i] = number[i].floatValue(); 
		}

		return results; 
	}

	@Override
	public Batchifier getBatchifier() {
		// The Batchifier describes how to combine a batch together
		// Stacking, the most common batchifier, takes N [X1, X2, ...] arrays to a single [N, X1, X2, ...] array
		return Batchifier.STACK;
	}
};

