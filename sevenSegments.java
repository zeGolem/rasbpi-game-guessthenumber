import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class sevenSegments {

    final static GpioController gpio = GpioFactory.getInstance();

    final static GpioPinDigitalOutput SEGDISP_TOP = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23);
    final static GpioPinDigitalOutput SEGDISP_TOP_LEFT = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24);
    final static GpioPinDigitalOutput SEGDISP_TOP_RIGHT = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22);
    final static GpioPinDigitalOutput SEGDISP_MIDDLE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25);
    final static GpioPinDigitalOutput SEGDISP_BOTTOM = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28);
    final static GpioPinDigitalOutput SEGDISP_BOTTOM_LEFT = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27);
    final static GpioPinDigitalOutput SEGDISP_BOTTOM_RIGHT = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29);
    
    final static GpioPinDigitalOutput LED_MORE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.LOW);
    final static GpioPinDigitalOutput LED_STDOUT = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, PinState.HIGH);
    final static GpioPinDigitalOutput LED_LESS = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW);
    
    final static GpioPinDigitalInput BUTTON_PLUS = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_UP);
    final static GpioPinDigitalInput BUTTON_OK = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_UP);
    final static GpioPinDigitalInput BUTTON_MINUS = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);
    
    static int currentNumberOnDisplay = -1;
    static int number = 0;
    static int randomNumber = 0;
    static int randomNumberCount = 0;
    static List<Integer> randomNumberList = new ArrayList<Integer>();
    static int lives = 3;
    
    static boolean shouldRefreshDisplay = true;

    public static void main(String[] args) throws InterruptedException {
    	BUTTON_PLUS.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent e) {
				if(e.getState().isHigh()) {
					if(number < 9)
						number ++;
				}
			}
    		
    	});
    	BUTTON_MINUS.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent e) {
				if(e.getState().isHigh()) {
					if(number > 0)
						number --;
				}
			}
    		
    	});
    	
    	BUTTON_OK.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent e) {
				if(e.getState().isHigh()) {
					System.out.println("Confirming number " + number);
					confirmNumber();
					
				}
			}
    		
    	});
    	
    	randomNumber = generateNumber();
    	randomNumberCount++;
    	randomNumberList.add(randomNumber);
    	
    	
    	while(true) {
    		if(currentNumberOnDisplay != number && shouldRefreshDisplay) {
    			System.out.println("Refresh !");
    			set7segNumber(number);
    			currentNumberOnDisplay = number;
    		}
    	}
    }
    
    public static void confirmNumber() {
    	if(number == randomNumber) {
    		System.out.println("Correct number ! (" + number + "==" + randomNumber + ")");
    		correctNumber();
    		
    		number = 0;
    		return;
    	} else {
    		System.out.println("Wrong number !");
    		wrongNumber();
    		number = 0;
    		return;
    	}
    	
    }
    
    
    
    public static void win() {
    	System.out.println("You won");
    	shouldRefreshDisplay = false;
    	reset7seg();
    	SEGDISP_TOP_LEFT.high();
    	SEGDISP_BOTTOM_LEFT.high();
    	SEGDISP_TOP_RIGHT.high();
    	SEGDISP_BOTTOM_RIGHT.high();
    	SEGDISP_BOTTOM.high();
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
    	randomNumberList = new ArrayList<Integer>();
    	randomNumber = generateNumber();
    	randomNumberCount = 1;
    	randomNumberList.add(randomNumber);
		lives = 3;
    	shouldRefreshDisplay = true;
    }
    public static void correctNumber() {
    	if(randomNumberCount >= 9) {
    		win();
    		return;
    	}
    	reset7seg();
    	shouldRefreshDisplay = false;
    	SEGDISP_TOP.high();
    	SEGDISP_TOP_LEFT.high();
    	SEGDISP_BOTTOM_LEFT.high();
    	SEGDISP_BOTTOM.high();
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		}
    	reset7seg();
    	set7segNumber(lives);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		}
    	for(int i = 0; i<3; i++) {
    		if(lives < 9) {
    			System.out.println("Getting a new life (" + lives + "<9)");
    			lives++;
        		set7segNumber(lives);
        		try {
    				Thread.sleep(500);
        		} catch (InterruptedException e1) {
    				
    				e1.printStackTrace();
    			}
    		} else {
    			set7segNumber(lives);
    			try {
    				Thread.sleep(500);
        		} catch (InterruptedException e1) {
    				
    				e1.printStackTrace();
    			}
    			set7segNumber(-1);
    			try {
    				Thread.sleep(500);
        		} catch (InterruptedException e1) {
    				
    				e1.printStackTrace();
    			}
    			set7segNumber(lives);
    			try {
    				Thread.sleep(1000);
        		} catch (InterruptedException e1) {
    				
    				e1.printStackTrace();
    			}
    			break;
    		}
    	}
    	number = 0;
    	randomNumber = generateNumber();
    	randomNumberCount++;
    	randomNumberList.add(randomNumber);
    	LED_MORE.low();
    	LED_LESS.low();
    	shouldRefreshDisplay = true;
    	
    }
    public static void wrongNumber() {
    	
    	if(number > randomNumber) {
    		System.out.println("Lower number : " + number + ">" + randomNumber);
    		shouldRefreshDisplay = false;
    		LED_MORE.low();
    		LED_LESS.high();
    		reset7seg();
    		SEGDISP_TOP.high();
        	SEGDISP_TOP_LEFT.high();
        	SEGDISP_MIDDLE.high();
        	SEGDISP_BOTTOM_LEFT.high();
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
        	reset7seg();
        	try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
        	set7segNumber(lives);
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
        	lives--;
        	set7segNumber(lives);
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
    		
    		shouldRefreshDisplay = true;
    		
    	} else if(number < randomNumber) {
    		System.out.println("Higher number : " + number + "<" + randomNumber);
    		shouldRefreshDisplay = false;
    		LED_MORE.high();
    		LED_LESS.low();
    		reset7seg();
    		SEGDISP_TOP.high();
        	SEGDISP_TOP_LEFT.high();
        	SEGDISP_MIDDLE.high();
        	SEGDISP_BOTTOM_LEFT.high();
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
        	reset7seg();
        	try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
        	set7segNumber(lives);
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
        	lives--;
        	set7segNumber(lives);
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
    		
    		shouldRefreshDisplay = true;
    		
    	}
    	if(lives == 0)
    	{
    		lost();
    	}
    }
    public static void lost() {
    	System.out.println("You lost");
    	shouldRefreshDisplay = false;
    	reset7seg();
    	SEGDISP_TOP.high();
    	SEGDISP_TOP_LEFT.high();
    	SEGDISP_TOP_RIGHT.high();
    	SEGDISP_MIDDLE.high();
    	SEGDISP_BOTTOM_LEFT.high();
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
    	randomNumberList = new ArrayList<Integer>();
    	randomNumber = generateNumber();
    	randomNumberCount = 1;
    	randomNumberList.add(randomNumber);
		lives = 3;
		shouldRefreshDisplay = true;
    	
    }
    
    public static int generateNumber() {
    	Random rand = new Random();
    	boolean generateNumber = true;
    	int finalNumber = 0;
    	while(generateNumber) {
    		int n = rand.nextInt(10);
    		generateNumber = false;
    		for(int i : randomNumberList) {
    			if(n == i) {
    				generateNumber = true;
    			}
    		}
    		finalNumber = n;
    	}
    	System.out.println("Randomly picked : " + finalNumber);
    	return finalNumber;
    }
    

    
    
    
    //7Segment
    
    
    
    
    
    public static void set7segNumber(int number) {
        reset7seg();
        switch (number) {
            case 0:
            	SEGDISP_TOP.high();
                SEGDISP_TOP_LEFT.high();
                SEGDISP_TOP_RIGHT.high();
                SEGDISP_BOTTOM_LEFT.high();
                SEGDISP_BOTTOM_RIGHT.high();
                SEGDISP_BOTTOM.high();
                break;
            case 1:
            	SEGDISP_TOP_RIGHT.high();
                SEGDISP_BOTTOM_RIGHT.high();
                break;
            case 2:
            	SEGDISP_TOP.high();
                SEGDISP_MIDDLE.high();
                SEGDISP_BOTTOM.high();
                SEGDISP_TOP_RIGHT.high();
                SEGDISP_BOTTOM_LEFT.high();
                break;
            case 3:
            	SEGDISP_MIDDLE.high();
                SEGDISP_TOP.high();
                SEGDISP_BOTTOM.high();
                SEGDISP_BOTTOM_RIGHT.high();
                SEGDISP_TOP_RIGHT.high();
                break;
            case 4:
            	SEGDISP_TOP_LEFT.high();
                SEGDISP_TOP_RIGHT.high();
                SEGDISP_BOTTOM_RIGHT.high();
                SEGDISP_MIDDLE.high();
                break;
            case 5:
            	SEGDISP_MIDDLE.high();
                SEGDISP_TOP.high();
                SEGDISP_BOTTOM.high();
                SEGDISP_TOP_LEFT.high();
                SEGDISP_BOTTOM_RIGHT.high();
                break;
            case 6:
            	SEGDISP_MIDDLE.high();
                SEGDISP_TOP.high();
                SEGDISP_BOTTOM.high();
                SEGDISP_TOP_LEFT.high();
                SEGDISP_BOTTOM_LEFT.high();
                SEGDISP_BOTTOM_RIGHT.high();
                break;
            case 7:
            	SEGDISP_TOP.high();
                SEGDISP_BOTTOM_RIGHT.high();
                SEGDISP_TOP_RIGHT.high();
                break;
            case 8:
            	SEGDISP_BOTTOM.high();
                SEGDISP_BOTTOM_LEFT.high();
                SEGDISP_BOTTOM_RIGHT.high();
                SEGDISP_MIDDLE.high();
                SEGDISP_TOP.high();
                SEGDISP_TOP_LEFT.high();
                SEGDISP_TOP_RIGHT.high();
                break;
            case 9:
            	SEGDISP_BOTTOM.high();
                SEGDISP_BOTTOM_RIGHT.high();
                SEGDISP_MIDDLE.high();
                SEGDISP_TOP.high();
                SEGDISP_TOP_LEFT.high();
                SEGDISP_TOP_RIGHT.high();
                break;
            default:
                break;
        }
    }
    public static void reset7seg() {
        
        SEGDISP_TOP.low();
        SEGDISP_TOP_LEFT.low();
        SEGDISP_TOP_RIGHT.low();
        SEGDISP_MIDDLE.low();
        SEGDISP_BOTTOM.low();
        SEGDISP_BOTTOM_LEFT.low();
        SEGDISP_BOTTOM_RIGHT.low();

    }

    


}