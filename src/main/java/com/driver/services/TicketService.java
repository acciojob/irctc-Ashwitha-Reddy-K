package com.driver.services;




import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db

        Train train=trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int bookedSeats=0;
        List<Ticket>booked=train.getBookedTickets();
        for(Ticket ticket:booked){
            bookedSeats+=ticket.getPassengersList().size();
        }

        if(bookedSeats+bookTicketEntryDto.getNoOfSeats()> train.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        String stations[] = train.getRoute().split(",");
        List<Passenger>passengerList = new ArrayList<>();

        for(int id: bookTicketEntryDto.getPassengerIds()){
            passengerList.add(passengerRepository.findById(id).get());
        }

        int sp = -1;
        int ep = -1;

        for(int i=0;i<stations.length;i++){
            if(bookTicketEntryDto.getFromStation().toString().equals(stations[i])){
                sp = i;
                break;
            }
        }
        for(int i=0;i<stations.length;i++){
            if(bookTicketEntryDto.getToStation().toString().equals(stations[i])){
                ep = i;
                break;
            }
        }

        if(sp == -1 || ep == -1 || ep - sp < 0){
            throw new Exception("Invalid stations");
        }

        // now generate ticket
        Ticket ticket=new Ticket();
        ticket.setPassengersList(passengerList);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        int fare = 0;
        fare = bookTicketEntryDto.getNoOfSeats() * (ep-sp)*300;

        ticket.setTotalFare(fare);
        ticket.setTrain(train);

        train.getBookedTickets().add(ticket);

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);

        trainRepository.save(train);

        return ticketRepository.save(ticket).getTicketId();
    }
}