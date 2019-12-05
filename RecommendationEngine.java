import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;

public class RecommendationEngine {

	public static int daily = 40;
	public static int days = 5;
	public static int population = 200; //total user should be equal to the total order if synthetic data is used 
	public static int anaYemek = 8;
	public static int araSicak = 4;
	public static int drink = 5;
	public static int dessert = 4;
	public static int starter = 3;
	public static int activeThreshold = 8; //active thresholdu
	//public static int totalRating = anaYemek+araSicak+drink+dessert+starter; //valid if data sets are used
	public static int totalRating = 24;
	public static int removeSize = 10;
	public static int totalOrder = daily*days;
	public static int k=10; //number of neighbors
	private static final String FILE_NAME = "jesterDataSet.xlsx";
	public static int numberOfRecommendations=7; //number of recommendations
	public static boolean syn=false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(syn){
			Object[][] pastOrders=createPastOrdersMatrix();
			
			Map<String,Object[]> utilityMatrixR=obtainUtilityMatrixR(pastOrders);
			Map<String,Object[]> utilityMatrixRR=obtainUtilityMatrixR(pastOrders);
			Map<String,Object[]> utilityMatrixT=obtainUtilityMatrixT(pastOrders);
			Map<String,Object[]> utilityMatrixTT=obtainUtilityMatrixT(pastOrders);
			//printUtilityMatrices(utilityMatrixR,utilityMatrixT);
			//System.out.println();
			int[] topRatedItems = topRatedItems(utilityMatrixR);
			/*for(int i=0; i<topRatedItems.length; i++){
				System.out.print(topRatedItems[i]+" ");
			}
			System.out.println();*/
			/*for(String user:utilityMatrixT.keySet())
			System.out.println(user+" is "+isActiveUser(user,utilityMatrixT));*/
	
	//		int[] topRatedItems = topRatedItems(utilityMatrixR);
	//		for(int i=0; i<topRatedItems.length; i++){
	//		System.out.print(topRatedItems[i]+" ");
	//		}
			
			//System.out.println();
			Map<String,Object[]> normalizedR=normalizeRatings(utilityMatrixR);
			//System.out.println();
			Map<String,double[]> normalizedDoubleR=convertDouble(normalizedR);
			//System.out.println();
			
			
			
				Map<String,Double> kNearestNeighbors=findNeighbors(normalizedDoubleR,"user"+1);
				Map<String,Object[]> kNeigborsUtilityR=findUtilityMatrixRofNeighbors(kNearestNeighbors,utilityMatrixRR);
				Map<Integer,Double> ratings=calculateRatings(kNearestNeighbors,kNeigborsUtilityR,utilityMatrixRR,"user"+1);
				Map<Integer,Double> ratingsTaste=calculateRatingsTaste(utilityMatrixTT,kNearestNeighbors,kNeigborsUtilityR,utilityMatrixRR,"user"+1);
				
				Iterator<Integer> itr=ratingsTaste.keySet().iterator();
				ArrayList<Double> values = new ArrayList<Double>();
				while(itr.hasNext()){
					int s=itr.next();
					//System.out.println("item "+(s+1)+" rating "+ratingsTaste.get(s));
					values.add(ratingsTaste.get(s));
				}
				Collections.sort(values);
				//System.out.println();
				int begin = 1;
				boolean[] isRecommended = new boolean[totalRating];
				int anaYemekMax = 2;
				int araSicakMax = 2;
				int drinkMax = 1;
				int dessertMax = 1;
				int starterMax = 1;
				int anaYemekR = 0;
				int araSicakR = 0;
				int drinkR = 0;
				int dessertR = 0;
				int starterR = 0;
				
				for(int i=values.size()-1; i>=0; i--){
					double rate = values.get(i);
					int index = -1;
					for(int key:ratingsTaste.keySet()){
						if(ratingsTaste.get(key)==rate && !isRecommended[key]) {
							index = key;
							isRecommended[key] = true;
							break;
						}
					}
					if(index != -1){
						if(index+1 >= 1 && index+1 <= 8 && anaYemekR < anaYemekMax){
							anaYemekR++;
							//System.out.println("item "+(index+1)+" is recommended as Main Course.");
						}
						else if(index+1 >= 9 && index+1 <= 12 && araSicakR < araSicakMax){
							araSicakR++;
							//System.out.println("item "+(index+1)+" is recommended as Appetizer.");
							}
						else if(index+1 >= 13 && index+1 <= 17 && drinkR < drinkMax){
							drinkR++;
							//System.out.println("item "+(index+1)+" is recommended as Drink.");
							}
						else if(index+1 >= 18 && index+1 <= 21 && dessertR < dessertMax){
							dessertR++;
							//System.out.println("item "+(index+1)+" is recommended as Dessert.");
							}
						else if(index+1 >= 22 && index+1 <= 24 && starterR < starterMax){
							starterR++;
							//System.out.println("item "+(index+1)+" is recommended as Entreé.");
							}
					}
					
					begin++;
					if(begin > numberOfRecommendations) break;
				}
 				/*System.out.println();
				Iterator<Integer> itrr=ratingsTaste.keySet().iterator();
				while(itrr.hasNext()){
					int s=itrr.next();
					System.out.println("item "+(s+1)+" rating "+ratingsTaste.get(s));
				}*/
		}
		else{
			double[][] desiredMatrix=desiredMatrix(ratingsFromDataset(totalOrder),totalRating,syn);
			//double[][] desiredMatrix=desiredMatrix(randomDataset(totalOrder),totalRating,true);
			double[][] desiredMatrixInitial=desiredMatrix;
			Object[][] pastOrders=generateMatrix(desiredMatrix,removeSize);
			Map<String,Object[]> utilityMatrixR=obtainUtilityMatrixR(pastOrders);
			Map<String,Object[]> utilityMatrixRR=obtainUtilityMatrixR(pastOrders);
			Map<String,Object[]> utilityMatrixT=obtainUtilityMatrixT(pastOrders);
			//System.out.println();
			//printUtilityMatrices(utilityMatrixR,utilityMatrixT);
			//System.out.println();
			Map<String,Object[]> normalizedR=normalizeRatings(utilityMatrixR);
			//System.out.println();
			Map<String,double[]> normalizedDoubleR=convertDouble(normalizedR);
			//System.out.println();
			
			int counter=0;
			int counter2=0;
			double error=0;
			double error2=0;
			double indError=0;
			double indError2=0;
			int accuracy[]=new int[2];
			int accuracyPairwise[]=new int[2];
			for(int i=0;i<population;i++){
				
				Map<String,Double> kNearestNeighbors=findNeighbors(normalizedDoubleR,"user"+(i+1));
				Map<String,Object[]> kNeigborsUtilityR=findUtilityMatrixRofNeighbors(kNearestNeighbors,utilityMatrixRR);
				Map<Integer,Double> ratings=calculateRatings(kNearestNeighbors,kNeigborsUtilityR,utilityMatrixRR,"user"+(i+1));
				
				indError=calculateRMSE(ratings,desiredMatrixInitial,i,numberOfRecommendations);
				indError2=calculateReverseRMSE(ratings,desiredMatrixInitial,i,numberOfRecommendations);
				accuracy[0]=accuracy[0]+calculateBinaryError(ratings,desiredMatrixInitial,i,numberOfRecommendations)[0];
				accuracy[1]=accuracy[1]+calculateBinaryError(ratings,desiredMatrixInitial,i,numberOfRecommendations)[1];
				accuracyPairwise[0]=accuracyPairwise[0]+calculatePairwiseBinaryError(ratings,desiredMatrixInitial,i,numberOfRecommendations)[0];
				accuracyPairwise[1]=accuracyPairwise[1]+calculatePairwiseBinaryError(ratings,desiredMatrixInitial,i,numberOfRecommendations)[1];
				
				if(indError!=0){
					counter++;
					error=error+indError;
				}
				if(indError2!=0){
					counter2++;
					error2=error2+indError2;
				}
			}
			//System.out.println("RMSE OVER 5 "+Math.sqrt((error/counter/numberOfRecommendations)));
			System.out.println("RMSE OVER 1 "+Math.sqrt((error/counter/numberOfRecommendations/25)));
			
			//System.out.println("REVERSE RMSE OVER 5 "+Math.sqrt((error2/counter2/numberOfRecommendations)));
			System.out.println("REVERSE RMSE OVER 1 "+Math.sqrt((error2/counter2/numberOfRecommendations/25)));
			
			System.out.println("GROUP BINARY ACCURACY "+(double)((double)accuracy[1]/(double)(accuracy[0]+(double)accuracy[1])));
			System.out.println("PAIRWISE BINARY ACCURACY "+(double)((double)accuracyPairwise[1]/((double)accuracyPairwise[0]+(double)accuracyPairwise[1])));
		
		}

	}
	
	//give userID and utility matrix T
	public static boolean isActiveUser(String user, Map<String,Object[]> map){
		Object[] orders = map.get(user);
		int total = 0;
		for(int i=0; i<orders.length; i++){
			total += (int)orders[i];
		}
		return total >= activeThreshold;
	}
	
	public static int[] topRatedItems(Map<String,Object[]> m){
		double[] averageRatings = new double[totalRating];
		int[] numberOfRates = new int[totalRating];
		for(String user:m.keySet()){
			Object[] ratings = m.get(user);
			for(int i=0; i<totalRating; i++){
				double r = 0;
				if(ratings[i].toString() != " "){
					r = (double)ratings[i];
					numberOfRates[i]++;
				}
				averageRatings[i] += r;
			}
		}
		for(int i=0; i<totalRating; i++){
			if(numberOfRates[i]==0) numberOfRates[i]++;
			averageRatings[i] = averageRatings[i] / numberOfRates[i];
			//System.out.printf("%.2f  ",averageRatings[i]);
		}
		//System.out.println();
		double[] anaYemekRate = new double[anaYemek];
		double[] araSicakRate = new double[araSicak];
		double[] drinkRate = new double[drink];
		double[] dessertRate = new double[dessert];
		double[] starterRate = new double[starter];
		for(int i=0; i<anaYemek; i++){
			anaYemekRate[i] = averageRatings[i];
		}
		for(int i=0; i<araSicak; i++){
			araSicakRate[i] = averageRatings[i+anaYemek];
		}
		for(int i=0; i<drink; i++){
			drinkRate[i] = averageRatings[i+anaYemek+araSicak];
		}
		for(int i=0; i<dessert; i++){
			dessertRate[i] = averageRatings[i+anaYemek+araSicak+drink];
		}
		for(int i=0; i<starter; i++){
			starterRate[i] = averageRatings[i+anaYemek+araSicak+drink+dessert];
		}
		int max1 = 0; //anaYemek1
		int max2 = 0; //anaYemek2 
		int max3 = 0; //araSıcak1
		int max4 = 0; //araSıcak2
		int max5 = 0; //drink
		int max6 = 0; //dessert
		int max7 = 0; //starter
		Arrays.sort(anaYemekRate);
		Arrays.sort(araSicakRate);
		Arrays.sort(drinkRate);
		Arrays.sort(dessertRate);
		Arrays.sort(starterRate);
		for(int i=0; i<totalRating; i++){
			if(averageRatings[i] == anaYemekRate[anaYemek-1] && i<anaYemek) max1 = i;
			if(averageRatings[i] == anaYemekRate[anaYemek-2] && i<anaYemek) max2 = i;
			if(averageRatings[i] == araSicakRate[araSicak-1] && i>=anaYemek && i<anaYemek+araSicak) max3 = i;
			if(averageRatings[i] == araSicakRate[araSicak-2] && i>=anaYemek && i<anaYemek+araSicak) max4 = i;
			if(averageRatings[i] == drinkRate[drink-1] && i>=anaYemek+araSicak && i<anaYemek+araSicak+drink) max5 = i;
			if(averageRatings[i] == dessertRate[dessert-1] && i>=anaYemek+araSicak+drink && i<anaYemek+araSicak+drink+dessert) max6 = i;
			if(averageRatings[i] == starterRate[starter-1] && i>=anaYemek+araSicak+drink+dessert && i<anaYemek+araSicak+drink+dessert+starter) max7 = i;
		}
		int[] indices = {max1,max2,max3,max4,max5,max6,max7};
		return indices;
	}
	
	public static int[] topOrderedItems(Map<String,Object[]> map){
		int[] totalOrders = new int[totalRating];
		for(String user:map.keySet()) {
			Object[] orders = map.get(user);
			for(int i=0; i<totalRating; i++){
				totalOrders[i] += (int)orders[i];
			}
		}
		int[] anaYemekRate = new int[anaYemek];
		int[] araSicakRate = new int[araSicak];
		int[] drinkRate = new int[drink];
		int[] dessertRate = new int[dessert];
		int[] starterRate = new int[starter];
		/*for(int i=0; i<totalRating; i++){
			System.out.print(totalOrders[i]+" ");
		}
		System.out.println();*/
		for(int i=0; i<anaYemek; i++){
			anaYemekRate[i] = totalOrders[i];
		}
		for(int i=0; i<araSicak; i++){
			araSicakRate[i] = totalOrders[i+anaYemek];
		}
		for(int i=0; i<drink; i++){
			drinkRate[i] = totalOrders[i+anaYemek+araSicak];
		}
		for(int i=0; i<dessert; i++){
			dessertRate[i] = totalOrders[i+anaYemek+araSicak+drink];
		}
		for(int i=0; i<starter; i++){
			starterRate[i] = totalOrders[i+anaYemek+araSicak+drink+dessert];
		}
		int max1 = 0; //anaYemek1
		int max2 = 0; //anaYemek2 
		int max3 = 0; //araSıcak1
		int max4 = 0; //araSıcak2
		int max5 = 0; //drink
		int max6 = 0; //dessert
		int max7 = 0; //starter
		Arrays.sort(anaYemekRate);
		Arrays.sort(araSicakRate);
		Arrays.sort(drinkRate);
		Arrays.sort(dessertRate);
		Arrays.sort(starterRate);
		for(int i=0; i<totalRating; i++){
			if(totalOrders[i] == anaYemekRate[anaYemek-1] && i<anaYemek) max1 = i;
			if(totalOrders[i] == anaYemekRate[anaYemek-2] && i<anaYemek) max2 = i;
			if(totalOrders[i] == araSicakRate[araSicak-1] && i>=anaYemek && i<anaYemek+araSicak) max3 = i;
			if(totalOrders[i] == araSicakRate[araSicak-2] && i>=anaYemek && i<anaYemek+araSicak) max4 = i;
			if(totalOrders[i] == drinkRate[drink-1] && i>=anaYemek+araSicak && i<anaYemek+araSicak+drink) max5 = i;
			if(totalOrders[i] == dessertRate[dessert-1] && i>=anaYemek+araSicak+drink && i<anaYemek+araSicak+drink+dessert) max6 = i;
			if(totalOrders[i] == starterRate[starter-1] && i>=anaYemek+araSicak+drink+dessert && i<anaYemek+araSicak+drink+dessert+starter) max7 = i;
		}
		int[] indices = {max1,max2,max3,max4,max5,max6,max7};
		return indices;
	}
	
	public static double[][] randomDataset(int size){
		double[][] dataMatrix = new double[size][101];
		Random rand = new Random();
		int rows = size;
		int columns = 101;
		for(int r=0; r<rows; r++){
			for(int c=0; c<columns; c++){
				dataMatrix[r][c] = rand.nextInt(6) * 1.0; //0-5 integer
				//if(r==0) System.out.print(dataMatrix[r][c]+" ");
				//System.out.println(dataMatrix[r][c]);
				//dataMatrix[r][c] = rand.nextDouble() * 5; //0-5 double
			}
		}
		return dataMatrix;
	}
	
	public static double[][] ratingsFromDataset(int size){
		double[][] dataMatrix = new double[size][101];
		try {
			
            FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
            
            int i = 0;
            while (iterator.hasNext()) {
            	if(i==size) break;
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                
                int j = 0;
                while (cellIterator.hasNext()) {

                    Cell currentCell = cellIterator.next();
                    //getCellTypeEnum shown as deprecated for version 3.15
                    //getCellTypeEnum ill be renamed to getCellType starting from version 4.0
                    if (currentCell.getCellTypeEnum() == CellType.STRING) {
                        //System.out.print(currentCell.getStringCellValue() + "--");
                    } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                        double value = currentCell.getNumericCellValue();
                        dataMatrix[i][j] = value;
                        j++;
                    	//System.out.print(currentCell.getNumericCellValue() + "--");
                    }
                }
                i++;
                //System.out.println();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return dataMatrix;
	}
	
	public static double convert(double d){
		return Math.round((d+10)/4) * 1.0;
	}
	
	public static double[][] desiredMatrix(double[][] dataMatrix, int jesterSize, boolean isRandom){
		int dataSize = dataMatrix.length;
		int columnSize = dataMatrix[0].length;
		int[] randomIndices = distinctRandomNumbers(jesterSize,columnSize);
		double[][] desiredMatrix = new double[dataSize][jesterSize];
		for(int i=0; i<dataSize; i++){
			for(int j=0; j<jesterSize; j++){
				int index = randomIndices[j];
				if(index==0) index++;
				if(!isRandom) desiredMatrix[i][j] = convert(dataMatrix[i][index]);
				else desiredMatrix[i][j] = dataMatrix[i][index];
			}
		}
		return desiredMatrix;
	}
	
	public static int[] distinctRandomNumbers(int k, int size){
		int[] randomValues = new int[k];
		Random rand = new Random();
		for(int i=0; i<k; i++){
			randomValues[i] = rand.nextInt(size/k) + (size/k)*i;
		}
		return randomValues;
		}
	
	
	public static Object[][] generateMatrix(double[][] desiredMatrix, int removal){
		int columnSize = desiredMatrix[0].length;
		int rowSize = desiredMatrix.length;
		Object[][] generatedMatrix = new Object[rowSize][2*columnSize+1];
		String name = "user";
		Random rand = new Random();
		for(int i=0; i<rowSize; i++){
			generatedMatrix[i][0] = name+(i+1); //her user 1 kere yiyormuş gibi
			//generatedMatrix[i][0] = name+(rand.nextInt(100)+1); //bazı userlar 1den fazla yapabilir
			int[] removeIndex = distinctRandomNumbers(removal,columnSize);
			int start = 0;
			for(int j=1; j<columnSize+1; j++){
				if(j==removeIndex[start]+1){
					generatedMatrix[i][j+columnSize] = " ";
					generatedMatrix[i][j] = 0;
					if(start < removal-1) start++;
				}
				else{
					generatedMatrix[i][j+columnSize] = desiredMatrix[i][j-1];
					generatedMatrix[i][j] = 1;//rand.nextInt(4)+1; //şurayı random sayı alsak sanki 1den fazla yemiş gibi olur
				}
			}
		}
		return generatedMatrix;
	}
	
	
	
	//checks if top ith item in the desired data =? top ith item in actual 
	public static int[] calculatePairwiseBinaryError(Map<Integer,Double> ratings,double[][] desiredMatrixInitial,int user, int numberOfRecom){
		int[] accuracy = new int[2];
		int falsePrediction=0;
		int truePrediction=0;
		
		Map<Integer,Double> desiredRatings=new TreeMap<>();
		Iterator<Integer> itr=ratings.keySet().iterator();
		while(itr.hasNext()){
			Integer i=itr.next();		
			desiredRatings.put(i, desiredMatrixInitial[user][i]);
		}
		Map<Integer,Double> sortedDesiredRatings=sortByComparator2(desiredRatings,false);
		//sorts ratings in descending order
		Map<Integer,Double> sortedRatings=sortByComparator2(ratings,false);
		
		int counter=0;
		Iterator<Integer> itr2=sortedDesiredRatings.keySet().iterator();
		Iterator<Integer> itr3=sortedRatings.keySet().iterator();
		while(itr2.hasNext() && itr3.hasNext()){
			int s1=itr2.next();
			int s2=itr3.next();
			//System.out.println("   "+s1+"   "+s2);
			if(s1==s2){
				truePrediction++;
				//System.out.println("true");
			}	
			else{
				falsePrediction++;
				//System.out.println("wrong");
			}
			counter++;
			if(counter==numberOfRecom){
				break;
			}
		}
		
		accuracy[0]=falsePrediction;
		accuracy[1]=truePrediction;
		
		return accuracy;
	}
	
	//finds how many top x items in the desired data recommended according to our prediction
	public static int[] calculateBinaryError(Map<Integer,Double> ratings,double[][] desiredMatrixInitial,int user, int numberOfRecom){
		int[] accuracy = new int[2];
		int falsePrediction=0;
		int truePrediction=0;
		
		Map<Integer,Double> desiredRatings=new TreeMap<>();
		Map<Integer,Double> kSortedRatings=new TreeMap<>();
		Iterator<Integer> itr=ratings.keySet().iterator();
		while(itr.hasNext()){
			Integer i=itr.next();		
			desiredRatings.put(i, desiredMatrixInitial[user][i]);
		}
		Map<Integer,Double> sortedDesiredRatings=sortByComparator2(desiredRatings,false);
		//sorts ratings in descending order
		Map<Integer,Double> sortedRatings=sortByComparator2(ratings,false);
		
		int counter=0;
		Iterator<Integer> itr1=sortedRatings.keySet().iterator();
		while(itr1.hasNext()){
			int s=itr1.next();			
			kSortedRatings.put(s, sortedRatings.get(s));	
			counter++;
			if(counter==numberOfRecom){
				break;
			}
		}
		
		
		counter=0;
		Iterator<Integer> itr2=sortedDesiredRatings.keySet().iterator();
		while(itr2.hasNext()){
			int s=itr2.next();			
			if(kSortedRatings.containsKey(s)){
				truePrediction++;
				//System.out.println("true");
			}	
			else{
				falsePrediction++;
				//System.out.println("wrong");
			}
			counter++;
			if(counter==numberOfRecom){
				break;
			}
		}
		
		accuracy[0]=falsePrediction;
		accuracy[1]=truePrediction;
		
		return accuracy;
	}
	
	// finds error according to top x items in the desired data
	public static double calculateReverseRMSE(Map<Integer,Double> ratings,double[][] desiredMatrixInitial,int user, int numberOfRecom){
		double error=0;
		double total=0;
		
		Map<Integer,Double> desiredRatings=new TreeMap<>();
		Iterator<Integer> itr=ratings.keySet().iterator();
		while(itr.hasNext()){
			Integer i=itr.next();		
			desiredRatings.put(i, desiredMatrixInitial[user][i]);
		}
		
		Map<Integer,Double> sortedDesiredRatings=sortByComparator2(desiredRatings,false);
		//selects top numberOfRecom ratings and calculates RMSE accordingly
		int counter=0;
		Iterator<Integer> itr2=sortedDesiredRatings.keySet().iterator();
		while(itr2.hasNext()){
			int s=itr2.next();			
			total=total+Math.pow((sortedDesiredRatings.get(s)-ratings.get(s)), 2);	
			//System.out.println("desired "+desiredMatrixInitial[user][s]);
			counter++;
			if(counter==numberOfRecom){
				break;
			}
		}		
		
		error=total;
		return error;
	}
	
	// finds error according to top x items in the actual data
	public static double calculateRMSE(Map<Integer,Double> ratings,double[][] desiredMatrixInitial,int user, int numberOfRecom){
		double error=0;
		double total=0;
		
		//sorts ratings in descending order
		Map<Integer,Double> sortedRatings=sortByComparator2(ratings,false);
		//selects top numberOfRecom ratings and calculates RMSE accordingly
		int counter=0;
		Iterator<Integer> itr=sortedRatings.keySet().iterator();
		while(itr.hasNext()){
			int s=itr.next();			
			//System.out.println("item "+(s+1)+"rating"+sortedRatings.get(s));
			total=total+Math.pow((desiredMatrixInitial[user][s]-sortedRatings.get(s)), 2);	
			//System.out.println("desired "+desiredMatrixInitial[user][s]);
			counter++;
			if(counter==numberOfRecom){
				break;
			}
		}
		//error=Math.sqrt(total/1);
		error=total;
		return error;
	}
	
	public static Map<Integer,Double> calculateRatings(Map<String,Double> kNearestNeighbors, Map<String,Object[]> kNeigborsUtilityR,Map<String,Object[]> utilityMatrixR, String user){
		double rating1=0; //sum of ratings of the item
		double rating2=0; //number of neighbors rated the item
		boolean check1=false;
		boolean check2=false;
		Map<Integer,Double> ratings=new TreeMap<>();
		
		for(int i=0; i<totalRating; i++){
			Object[] m=utilityMatrixR.get(user);
			//if the current user has not rated the item i yet
			if(m[i].toString()==" "){
				check1=true;
				Iterator<String> itr=kNearestNeighbors.keySet().iterator();
				while(itr.hasNext()){
					String s=itr.next();
					Object[] r=kNeigborsUtilityR.get(s);
					//if the neighbor rated the item i
					if(r[i].toString()!=" "){
						check2=true;
						//System.out.println("score"+kNearestNeighbors.get(s));
						//System.out.println("rating"+(double)r[i]);
						rating1=rating1+((kNearestNeighbors.get(s))*((double)r[i]));
						rating2=rating2+kNearestNeighbors.get(s);
					}
				}
			}
			//calculates rating of the item i
			//stores it in ratings
			if(check1 && check2){
				double rating=rating1/rating2;
				ratings.put(i, rating);
			}
			//resets variables
			rating1=0;
			rating2=0;
			check1=false;
			check2=false;
		}
		return ratings;
	}
	
	public static Map<Integer,Double> calculateRatingsTaste(Map<String,Object[]> utilityMatrixT, Map<String,Double> kNearestNeighbors, Map<String,Object[]> kNeigborsUtilityR,Map<String,Object[]> utilityMatrixR, String user){
		double rating1=0; //sum of ratings of the item
		double rating2=0; //number of neighbors rated the item
		boolean check1=false;
		boolean check2=false;
		int counter=0;
		Map<Integer,Double> ratings=new TreeMap<>();
		
		for(int i=0; i<totalRating; i++){
			Object[] m=utilityMatrixR.get(user);
			//if the current user has not rated the item i yet
			if(m[i].toString()==" "){
				check1=true;
				Iterator<String> itr=kNearestNeighbors.keySet().iterator();
				while(itr.hasNext()){
					String s=itr.next();
					//System.out.println("n"+s);
					Object[] r=kNeigborsUtilityR.get(s);
					//if the neighbor rated the item i
					if(r[i].toString()!=" "){
						check2=true;
						//System.out.println("score"+kNearestNeighbors.get(s));
						//System.out.println("rating"+(double)r[i]);
						
						Object[] tastes = utilityMatrixT.get(s);
						
						rating1=rating1+((kNearestNeighbors.get(s))*((double)r[i])*(int)tastes[i]);
						rating2=rating2+kNearestNeighbors.get(s);
						counter=counter+1;
					}
				}
			}
			//calculates rating of the item i
			//stores it in ratings
			if(check1 && check2){
				double rating=rating1/rating2;
				rating=rating/(double)counter;
				ratings.put(i, rating);
			}
			//resets variables
			rating1=0;
			rating2=0;
			check1=false;
			check2=false;
			counter=0;
		}
		return ratings;
	}
	
	//obtains utility matrix of k nearest neighbors
	public static Map<String,Object[]> findUtilityMatrixRofNeighbors(Map<String,Double> kNearestNeighbors,Map<String,Object[]> utilityMatrixR){
		Map<String,Object[]> kNeigborsUtilityR=new TreeMap<>();
		
		Iterator<String> itr=kNearestNeighbors.keySet().iterator();
		while(itr.hasNext()){
			String s=itr.next();
			
			kNeigborsUtilityR.put(s, utilityMatrixR.get(s));
		}
		
		return kNeigborsUtilityR;
	}
	
	//finds k nearest neighbors 
	//returns k nearest neighbors
	public static Map<String,Double> findNeighbors(Map<String,double[]> utilityMatrixR, String user){
		LinkedList<String> neighbors=new LinkedList<String>();
		Map<String,Double> similarityScores=new TreeMap<>();
		double[] u = utilityMatrixR.get(user);
		//finds similarity scores between current user and all users
		//stores them in similarityScores
		for(String str: utilityMatrixR.keySet()){
			if(!str.equals(user)){
				double[] n = utilityMatrixR.get(str);
				double similarity=cosineSimilarity(u,n);
				if(similarity!=0){
					similarityScores.put(str, similarity);
				}
			}
		}
		//sorts similarityScores according to similarity scores in descending order
		Map<String,Double> sortedSimilarityScores=sortByComparator(similarityScores,false);
		
		//finds k nearest neighbors 
		//stores them in neighbors 
		Iterator<String> itr=sortedSimilarityScores.keySet().iterator();
		int counter=0;
		while(itr.hasNext()){
			String s=itr.next();
			neighbors.add(s);
			//System.out.println("user "+s+" similarity score "+sortedSimilarityScores.get(s));
			counter++;
			if(counter==k){
				break;
			}
		}
		//obtains k nearest neighbors with userid and similarity score
		Map<String,Double> kNearestSimScores=new TreeMap<>();
		for(int i=0; i<neighbors.size(); i++){
			kNearestSimScores.put(neighbors.get(i), sortedSimilarityScores.get(neighbors.get(i)));
		}
		
		return kNearestSimScores;
	}
	
	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order){
        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>(){
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2){
                if(order){
                    return o1.getValue().compareTo(o2.getValue());
                }
                else{
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : list){
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	private static Map<Integer, Double> sortByComparator2(Map<Integer, Double> unsortMap, final boolean order){
        List<Entry<Integer, Double>> list = new LinkedList<Entry<Integer, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Double>>(){
            public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2){
                if(order){
                    return o1.getValue().compareTo(o2.getValue());
                }
                else{
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for (Entry<Integer, Double> entry : list){
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	//finds cosine similarity between two vectors
	//returns similarity score
	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    if( (Math.sqrt(normA) * Math.sqrt(normB))==0){
	    	return 0;
	    }
	    if(dotProduct==0 && (Math.sqrt(normA) * Math.sqrt(normB))==0){
	    	return 0;
	    }
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	//converts object to double
	//" " becomes 0 since this method is used after normalizeRatings
	public static Map<String,double[]> convertDouble(Map<String,Object[]> utilityMatrixR){
		Map<String,double[]> doubleUtilityMatrixR=new TreeMap<>();
		for(String str: utilityMatrixR.keySet()){
			Object[] m = utilityMatrixR.get(str);
			double[] d= new double[m.length];	
			for(int i=0; i<m.length;i++){	
				if(m[i].toString()!=" "){
					d[i]=(double)m[i];
				}
			}
			doubleUtilityMatrixR.put(str, d);
		}
		
		/*for(String str: doubleUtilityMatrixR.keySet()){
			double[] ratings = doubleUtilityMatrixR.get(str);
			System.out.printf("%-12s"+" ", str);
			for(int u=0; u<ratings.length; u++){
				System.out.printf("%.2f", ratings[u]);
				System.out.print("\t");
			}
			System.out.print("\n");
		}*/
		
		return doubleUtilityMatrixR;
	}
	
	//normalizes ratings
	//returns normalized ratings in the same format as input
	public static Map<String,Object[]> normalizeRatings(Map<String,Object[]> utilityMatrixR){
		for(String str: utilityMatrixR.keySet()){
			Object[] m = utilityMatrixR.get(str);
					
			int counter=0;
			double sum=0;
			for(int i=0; i<m.length;i++){				
				if(m[i].toString()!=" "){
					counter++;
					sum=sum+(double)m[i];
				}
			}
			double mean=sum/counter;
			for(int j=0; j<m.length; j++){
				if(m[j].toString()!=" "){
					m[j]=(double)m[j]-mean;
				}			
			}
			
			utilityMatrixR.replace(str, utilityMatrixR.get(str), m);
		}
		
		/*for(String str: utilityMatrixR.keySet()){
			Object[] ratings = utilityMatrixR.get(str);
			System.out.printf("%-12s"+" ", str);
			for(int u=0; u<ratings.length; u++){
				if(ratings[u].toString()==" " && u==0){
					System.out.print("\t"+"\t");
				}
				else if(ratings[u]!=" "){
					System.out.printf("%.2f", (double)ratings[u]);
					System.out.print("\t");
				}
				else{
					System.out.print("\t");
				}
			}
			System.out.print("\n");
		}*/
		
		return utilityMatrixR;
	}
	
	//prints utility matrix
	public static void printUtilityMatrices(Map<String,Object[]> utilityMatrixR,Map<String,Object[]> utilityMatrixT){
		for(String str: utilityMatrixR.keySet()){
			Object[] ratings = utilityMatrixR.get(str);
			System.out.printf("%-12s"+" ", str);
			for(int u=0; u<ratings.length; u++){
				if(ratings[u].toString()==" " && u==0){
					System.out.print("\t"+"\t");
				}
				else{
					System.out.print(ratings[u].toString()+"\t");
				}
			}
			System.out.print("\n");
		}
		System.out.println("###########################################");
		for(String str: utilityMatrixT.keySet()){
			Object[] ratings = utilityMatrixT.get(str);
			System.out.printf("%-12s"+" ", str);
			for(int u=0; u<ratings.length; u++){
				System.out.print(ratings[u].toString()+"  ");
			}
			System.out.print("\n");
		}
	}
	
	//obtains utility matrix for number of tastes
	//returns utilityMatrixT
	public static Map<String,Object[]> obtainUtilityMatrixT(Object[][] pastOrders){
		Map<String,Object[]> utilityMatrixT = new TreeMap<>();
		for(int o=0; o<daily*days; o++){
			String currentUser =(String) pastOrders[o][0];

			if(!utilityMatrixT.containsKey(currentUser)) {
				Object[] ratings = new Object[totalRating];
				for(int r=0; r<ratings.length; r++){
					ratings[r] = pastOrders[o][1+r];
				}
				utilityMatrixT.put(currentUser, ratings);
			}
			else {
				Object[] ratings = utilityMatrixT.get(currentUser);
				for(int r=0; r<ratings.length; r++){
					Object current = pastOrders[o][1+r];
					int before = (int) ratings[r];
					int after = (int) current;
					ratings[r] = before+after;
	
				}
				utilityMatrixT.put(currentUser, ratings);
			}
		}
		return utilityMatrixT;
	}
	
	//obtains utility matrix for ratings
	//returns utilityMatrixR
	public static Map<String,Object[]> obtainUtilityMatrixR(Object[][] pastOrders){
		Map<String,Object[]> utilityMatrixR = new TreeMap<>();
		for(int o=0; o<daily*days; o++){
			String currentUser =(String) pastOrders[o][0];
			
			////////FILLING UTILITY MATRIX R//////////////////////
			if(!utilityMatrixR.containsKey(currentUser)) {
				Object[] ratings = new Object[totalRating];
				for(int r=0; r<ratings.length; r++){
					ratings[r] = pastOrders[o][totalRating+1+r];
				}
				utilityMatrixR.put(currentUser, ratings);
			}
			else {
				Object[] ratings = utilityMatrixR.get(currentUser);
				for(int r=0; r<ratings.length; r++){
					Object current = pastOrders[o][totalRating+1+r];
					if(current.toString() != " ") {
						Object modified = ratings[r];
						if(modified.toString() != " "){
							double before = (double) modified;
							double after = (double) current;
							ratings[r] = (before+after)/2.0;
						}
						else {
							double after = (double) current;
							ratings[r] = after;
						}
					}
				}
				utilityMatrixR.put(currentUser, ratings);
			}
		}
		return utilityMatrixR;
	}
	
	//creates past orders matrix randomly
	//returns pastOrders object
	public static Object[][] createPastOrdersMatrix(){
		Random generator = new Random();
		ArrayList<Integer> selectedItems = new ArrayList<Integer>();
		Object[][] pastOrders = new Object[totalOrder][1+2*totalRating];
		boolean startedUser1 = false; //to ensure there is always user1
		for(int k=0; k<totalOrder; k++){
			int counter1 = 2;
			int counter2 = 2;
			int counter3 = 1;
			int counter4 = 1;
			int counter5 = 1;
			int start1 = 0;
			int start2 = 0;
			int start3 = 0;
			int start4 = 0;
			int start5 = 0;
			int userNo = generator.nextInt(population) + 1;
			String theUser = "";
			if(userNo != 1 && !startedUser1){
				theUser = "user"+1;
				startedUser1 = true;
			}
			else theUser = "user"+userNo;
			pastOrders[k][0] = theUser;
			
			//ana yemekler
			for(int y=1; y<=anaYemek; y++){
				if(start1 == counter1){
					while(y<=anaYemek){
						pastOrders[k][y] = 0;
						y++;
					}
					break;
				}
				int selected = generator.nextInt(2);
				if(selected==1){
					start1++;
					selectedItems.add(y);
				}
				pastOrders[k][y] = selected;
			}
			
			//ara sıcaklar
			for(int y=anaYemek+1; y<=anaYemek+araSicak; y++){
				if(start2 == counter2){
					while(y<=anaYemek+araSicak){
						pastOrders[k][y] = 0;
						y++;
					}
					break;
				}
				int selected = generator.nextInt(2);
				if(selected==1){
					selectedItems.add(y);
					start2++;
				}
				pastOrders[k][y] = selected;
			}
			
			//içecek
			for(int y=1+anaYemek+araSicak; y<=anaYemek+araSicak+drink; y++){
				if(start3 == counter3){
					while(y<=anaYemek+araSicak+drink){
						pastOrders[k][y] = 0;
						y++;
					}
					break;
				}
				int selected = generator.nextInt(2);
				if(selected==1){
					start3++;
					selectedItems.add(y);
				}
				pastOrders[k][y] = selected;
			}
			
			//tatli
			for(int y=1+anaYemek+araSicak+drink; y<=anaYemek+araSicak+drink+dessert; y++){
				if(start4 == counter4){
					while(y<=anaYemek+araSicak+drink+dessert){
						pastOrders[k][y] = 0;
						y++;
					}
					break;
				}
				int selected = generator.nextInt(2);
				if(selected==1){
					start4++;
					selectedItems.add(y);
				}
				pastOrders[k][y] = selected;
			}
			
			//starter
			for(int y=1+anaYemek+araSicak+drink+dessert; y<=anaYemek+araSicak+drink+dessert+starter; y++){
				if(start5 == counter5){
					while(y<=anaYemek+araSicak+drink+dessert+starter){
						pastOrders[k][y] = 0;
						y++;
					}
					break;
				}
				int selected = generator.nextInt(2);
				if(selected==1){
					start5++;
					selectedItems.add(y);
				}
				pastOrders[k][y] = selected;
			}
			
			//assigns Ratings
			for(int y=1+totalRating; y<=2*totalRating; y++){
				if(selectedItems.contains(y-totalRating)){
					double generated = generator.nextInt(6);
					pastOrders[k][y] = generated * 1.0;
				}
				else pastOrders[k][y] = " "; //empty
			}
			
			selectedItems = new ArrayList<Integer>();
		}
		/*for(int o=0; o<daily*days; o++){
				System.out.printf("%-12s"+" ", pastOrders[o][0]);
			for(int u=1; u<totalRating+1; u++){
				System.out.print(pastOrders[o][u].toString()+"  ");
			}
			for(int u=totalRating+1; u<2*totalRating+1; u++){
				System.out.print(pastOrders[o][u].toString()+"\t");	
			}
			System.out.print("\n");
		}*/
		return pastOrders;
	}

}

