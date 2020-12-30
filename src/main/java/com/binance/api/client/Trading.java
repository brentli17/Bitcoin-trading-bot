package com.binance.api.client;

import com.binance.api.client.domain.market.TickerStatistics;
import java.util.Scanner;

class Trading {
    private double previousPrice;   //price of btc in previous transaction
    private double currentPrice;    //current price of bitcoin
    private double usdtWallet;      //currently held $ in USD
    private double btcWallet;       //currently held $ in BTC
    private double transactionThreshold; //min change in bitcoin price to warrant a transaction
    private String lastTransaction; //last transaction type that was carried out (buy/sell)
    public boolean stopTrading;     //boolean to start and stop trading
    public long tradingFrequency;   //how often the bot will check the price of BTC (using binance api)

    //constructor
    public Trading(double previousPrice, double transactionThreshold, double usdtWallet, double btcWallet, long tradingFrequency){
        this.previousPrice = previousPrice;
        this.transactionThreshold = transactionThreshold;
        this.usdtWallet = usdtWallet;
        this.btcWallet = btcWallet;
        this.tradingFrequency = tradingFrequency;
        lastTransaction = "";
        currentPrice = 0.00;
        stopTrading = false;
    }

    public void tradingMain() throws InterruptedException {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
        BinanceApiRestClient client = factory.newRestClient();

        double change;

        //main loop. as long as this runs, the bot will continue to trade
        while(!stopTrading){
            TickerStatistics tickerStatistics = client.get24HrPriceStatistics("BTCUSDT");
            currentPrice = Double.parseDouble(tickerStatistics.getLastPrice()); //get current price

            change = currentPrice - previousPrice;
            System.out.println(currentPrice + " $" + change);

            if(Math.abs(change) > transactionThreshold){
                System.out.println();
                System.out.println("Current Price: " + currentPrice + "    Previous Price: " + previousPrice);
                if(lastTransaction.equals("buy") && (change > transactionThreshold)){
                    System.out.println("Selling...");
                    sell(currentPrice);
                    previousPrice = currentPrice;
                }
                else if(lastTransaction.equals("sell") && (change > transactionThreshold)){
                    System.out.println("Buying...");
                    buy(currentPrice);
                    previousPrice = currentPrice;
                }
                System.out.println();
            }
            Thread.sleep(tradingFrequency * 1000);    //wait 1 second
        }
    }

    public void sell(double price){
        lastTransaction = "sell";
        usdtWallet = btcWallet * currentPrice;
        System.out.println("Sold " + btcWallet + " bitcoin for $" + usdtWallet);
        btcWallet = 0.00;
    }

    public void buy(double price){
        lastTransaction = "buy";
        btcWallet = usdtWallet / currentPrice;
        System.out.println("Bought " + btcWallet + " bitcoin for $" + usdtWallet);
        usdtWallet = 0.00;
    }

    //mutator to start/stop trading (pretty much only used to stop trading)
    public void setStopTrading(boolean trading){
        stopTrading = trading;
    }
}