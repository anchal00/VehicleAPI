package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Implements the car service create, read, update or delete information about
 * vehicles, as well as gather related location and price data when desired.
 */
@Service
@Primary
public class CarService {

    @Autowired
    private  CarRepository repository;

    @Autowired
    private  WebClient maps;
    
    @Autowired
    private  WebClient pricing;

    // public CarService(CarRepository repository, WebClient mapsWebClient, WebClient pricesWebClient) {
    //     /**
    //      * TODO: Add the Maps and Pricing Web Clients you create in
    //      * `VehiclesApiApplication` as arguments and set them here.
    //      */
    //     this.mapsWebClient = mapsWebClient;
    //     this.pricesWebClient = pricesWebClient;
    //     this.repository = repository;
    // }

    /**
     * Gathers a list of all vehicles
     * 
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * 
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists. If it does not
         * exist, throw a CarNotFoundException Remove the below code as part of your
         * implementation.
         */
        Car car = getCarFromDb(id);
        if (car == null) {

            throw new CarNotFoundException("Cannot find car with id : " + id);
        }
        /**
         * TODO: Use the Pricing Web client you create in `VehiclesApiApplication` to
         * get the price based on the `id` input' TODO: Set the price of the car Note:
         * The car class file uses @transient, meaning you will need to call the pricing
         * service each time to get the price.
         */

        Price priceOfCar = pricing.get().uri("/services/price" + "?vehicleId=" + id).retrieve()
                .bodyToMono(Price.class).block();

        car.setPrice(priceOfCar.getPrice().toString());

        /**
         * TODO: Use the Maps Web client you create in `VehiclesApiApplication` to get
         * the address for the vehicle. You should access the location from the car
         * object and feed it to the Maps service. TODO: Set the location of the
         * vehicle, including the address information Note: The Location class file also
         * uses @transient for the address, meaning the Maps service needs to be called
         * each time for the address.
         */
        Location carLocation = car.getLocation();

        Address addressOfCar = maps.get()
                .uri("/maps?lat=" + carLocation.getLat() + "&lon=" + carLocation.getLon()).retrieve()
                .bodyToMono(Address.class).block();

        carLocation.setAddress(addressOfCar.getAddress());
        carLocation.setCity(addressOfCar.getCity());
        carLocation.setState(addressOfCar.getState());
        carLocation.setZip(addressOfCar.getZip());

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * 
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            System.out.println("Saving");
            return repository.findById(car.getId()).map(carToBeUpdated -> {
                carToBeUpdated.setDetails(car.getDetails());
                carToBeUpdated.setLocation(car.getLocation());
                return repository.save(carToBeUpdated);
            }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * 
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        /**
         * TODO: Find the car by ID from the `repository` if it exists. If it does not
         * exist, throw a CarNotFoundException
         */
        Car car = getCarFromDb(id);

        if (car == null) {
            throw new CarNotFoundException("Cannot find car with id : " + id);
        }

        repository.delete(car);
        /**
         * TODO: Delete the car from the repository.
         */

    }

    private Car getCarFromDb(Long id) {

        Optional<Car> optCar = repository.findById(id);
        if (optCar.isPresent())
            return optCar.get();
        return null;
    }
}
