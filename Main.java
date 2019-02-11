import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
	

	//SETUP

    final static GpioController gpio = GpioFactory.getInstance();

    final static GpioPinDigitalOutput LED_LESS = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21);
    final static GpioPinDigitalOutput LED_STDOUT = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_20);
    final static GpioPinDigitalOutput LED_MORE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16);
        
    final static GpioPinDigitalInput BUTTON_1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_12, PinPullResistance.PULL_UP);
    final static GpioPinDigitalInput BUTTON_2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_5, PinPullResistance.PULL_UP);
    final static GpioPinDigitalInput BUTTON_3 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_6, PinPullResistance.PULL_UP);
    final static GpioPinDigitalInput BUTTON_4 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_13, PinPullResistance.PULL_UP);
    final static GpioPinDigitalInput BUTTON_5 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_19, PinPullResistance.PULL_UP);
	final static GpioPinDigitalInput BUTTON_6 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_26, PinPullResistance.PULL_UP);

	static int randomNumber = generateNumber();
	static int[] seenNumbers = new int[6];
	static int generatedNumberCount = 0;
    static boolean isReadyToListenForInput = false;
    
    static int tries = 3;
    
    public static void main(String[] args) throws InterruptedException {
    
        
        
		System.out.println(randomNumber);
		seenNumbers[generatedNumberCount] = randomNumber;
    	
        //Add buttons listener
        BUTTON_1.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
				
				
				if(arg0.getState().isHigh()) {
					
					try {
						handleButtonPresses(1);
					} catch (Exception e) {
					}
					

					
				}
			}

        });
        
        BUTTON_2.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
				if(arg0.getState().isHigh()) {
					try {
						handleButtonPresses(2);
					} catch (Exception e) {
					}
				}
			}

        });
        
        BUTTON_3.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
				if(arg0.getState().isHigh()) {
					try {
						handleButtonPresses(3);
					} catch (Exception e) {
					}
					
				}
			}

        });
        
        BUTTON_4.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
				if(arg0.getState().isHigh()) {
					try {
						handleButtonPresses(4);
					} catch (Exception e) {
					}
				}
			}

        });
        
        BUTTON_5.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
				if(arg0.getState().isHigh()) {
						try {
							handleButtonPresses(5);
						} catch (Exception e) {
						}
					
				}
			}

        });
        
        BUTTON_6.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
				if(arg0.getState().isHigh()) {
					try {
						handleButtonPresses(6);
					} catch (Exception e) {
					}
				}
			}

        });
		//End of setup

        isReadyToListenForInput = true;
        
        LED_STDOUT.high();
		while(true){Thread.sleep(500);}
        

    }
	
	
	static void handleButtonPresses(int button) throws InterruptedException {
		//Turns off all LEDs
		LED_MORE.low();
		LED_LESS.low();
		System.out.println("pressed button " + button);
		System.out.println("[VARSTATE] tries="+tries+" randomNumber="+randomNumber+" generatedNumberCount="+generatedNumberCount);

		if(isReadyToListenForInput) {
			if(randomNumber == button) {
				correctNumber();
			} else {
				if(randomNumber > button){
					LED_MORE.high();
				}else {
					LED_LESS.high();
				}
				wrongNumber();
			}
		}
	}

	static void correctNumber() throws InterruptedException{
		//Turn off all the LEDs
		LED_MORE.low();
		LED_LESS.low();
		LED_STDOUT.low();
		Thread.sleep(500);

		//Correct Number animation
		LED_MORE.high();
		LED_LESS.high();
		LED_STDOUT.high();
		Thread.sleep(1000);
		LED_MORE.low();
		LED_LESS.low();
		LED_STDOUT.low();
		Thread.sleep(500);
		LED_MORE.high();
		LED_LESS.high();
		LED_STDOUT.high();
		Thread.sleep(1000);
		LED_MORE.low();
		LED_LESS.low();
		LED_STDOUT.low();
		
		//Testing for win
		if(generatedNumberCount >= 5) {
			try {
				win();

			} catch (Exception e) {
				//TODO: handle exception
			}
		}
		else {
			//Generating new number

			int newNumber = 0;
			boolean shouldContinue = true;
			while (shouldContinue) {
				shouldContinue = false;
				newNumber = generateNumber();
				for (int i : seenNumbers) {
					System.out.println("Comparing " + i + " and " + newNumber);
					if(newNumber == i){
						shouldContinue = true;

						System.out.println("Equal! Generating a new number");
					}
				}
				
			}
			//Adding tries & saving number in array
			tries+=2;
			System.out.println(newNumber);
			generatedNumberCount++;
			seenNumbers[generatedNumberCount] = newNumber;
			randomNumber = newNumber;
		}
		

	}

    static void win() throws InterruptedException {
    	isReadyToListenForInput = false;
    	//Win animation
    	System.out.println("Win !");
    	LED_MORE.low();
		LED_STDOUT.low();
    	LED_LESS.high();
    	Thread.sleep(500);
    	LED_LESS.low();
    	LED_STDOUT.high();
    	Thread.sleep(500);
    	LED_STDOUT.low();
    	LED_MORE.high();
    	Thread.sleep(500);
    	LED_MORE.low();
    	LED_STDOUT.high();
		
		//Reset
		generatedNumberCount = 0;
		seenNumbers = new int[6];
    	randomNumber = generateNumber();
    	tries = 3;
		System.out.println(randomNumber);
    	LED_STDOUT.high();
    	isReadyToListenForInput = true;
    }
    
    static void wrongNumber() throws InterruptedException{
		isReadyToListenForInput = false;
		//Wrong number animation
    	System.out.println("Wrong number");
    	LED_STDOUT.high();
    	Thread.sleep(500);
    	LED_STDOUT.low();
    	Thread.sleep(500);
    	//Removes a try
    	tries -- ;
    	
    	if(tries == 0) {
    		loose();
    	}
    	LED_STDOUT.high();
    	isReadyToListenForInput = true;
    }
    
    static void loose() {
		//Loosing animation
    	System.out.println("u lost u nub");
    	LED_STDOUT.high();
		LED_LESS.low();
		LED_MORE.low();



    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	LED_STDOUT.low();
    	//Reset
    	randomNumber = generateNumber();
		tries = 3;
		generatedNumberCount = 0;
		seenNumbers = new int[6];
		System.out.println(randomNumber);
		try {Thread.sleep(1000);}catch (InterruptedException e) {}
    	LED_STDOUT.high();
    	isReadyToListenForInput = true;
    	
	}
	
	static int generateNumber(){
		return ThreadLocalRandom.current().nextInt(1, 6 + 1);
	}

}
