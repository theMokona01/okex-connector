package classes.WebSocket.controller;


import classes.WebSocket.model.Ticker;
import classes.WebSocket.model.TickerUI;
import classes.WebSocket.repository.TickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

//restful web service
//@Controller
public class TickerController {
    @Autowired
    TickerRepository repository;

    //get method, create several tickers without passing any data, default option
    @GetMapping("/bulkcreate")
    public String bulkcreate() {
        // save a single ticker
        repository.save(new Ticker("ETH-USDT",221.5, 211.4, 211.4, 211.45, 207.2, 214.3, 206.2, 62449406.56, "2019-07-31T04:52:17.152Z"));

        return "Recorded ticker";
    }

    //post method, create single ticker by passing customer data as JSON
    @PostMapping("/create")
    public String create(@RequestBody TickerUI ticker){
        // save a single ticker
        repository.save(new Ticker(ticker.getInstrumentId(),ticker.getLast(), ticker.getBestBid(), ticker.getBestAsk(), ticker.getOpen24h(), ticker.getHigh24h(), ticker.getLow24h(), ticker.getBaseVolume(), ticker.getQuoteVolume(), ticker.getTimestamp()));

        return "Ticker is created";
    }
    //get method, search all tickers and returns as JSON
    @GetMapping("/findall")
    public List<TickerUI> findAll(){

        List<Ticker> tickers = repository.findAll();
        List<TickerUI> tickerUI = new ArrayList<>();

        for (Ticker ticker : tickers) {
            tickerUI.add(new TickerUI(ticker.getInstrumentId(),ticker.getLast(), ticker.getBestBid(), ticker.getBestAsk(), ticker.getOpen24h(), ticker.getHigh24h(), ticker.getLow24h(), ticker.getBaseVolume(), ticker.getQuoteVolume(), ticker.getTimestamp()));
        }

        return tickerUI;
    }
    //search by id
    @RequestMapping("/search/{id}")
    public String search(@PathVariable long id){
        String ticker = "";
        ticker = repository.findById(id).toString();
        return ticker;
    }

    //search by first name
    @RequestMapping("/searchbyinstrumentId/{instrumentId}")
    public List<TickerUI> fetchDataByInstrumentId(@PathVariable String instrumentId){

        List<Ticker> tickers = repository.findByInstrumentId(instrumentId);
        List<TickerUI> tickerUI = new ArrayList<>();

        for (Ticker ticker : tickers) {
            tickerUI.add(new TickerUI(ticker.getInstrumentId(),ticker.getLast(), ticker.getBestBid(), ticker.getBestAsk(), ticker.getOpen24h(), ticker.getHigh24h(), ticker.getLow24h(), ticker.getBaseVolume(), ticker.getQuoteVolume(), ticker.getTimestamp()));
        }

        return tickerUI;
    }
}
