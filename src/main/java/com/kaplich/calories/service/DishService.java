package com.kaplich.calories.service;

import com.kaplich.calories.cache.CacheEntity;
import com.kaplich.calories.dto.ClientDto;
import com.kaplich.calories.dto.DishDto;
import com.kaplich.calories.dto.ProductDto;
import com.kaplich.calories.mapper.ClientMapper;
import com.kaplich.calories.mapper.DishMapper;
import com.kaplich.calories.mapper.ProductMapper;
import com.kaplich.calories.model.Client;
import com.kaplich.calories.model.Dish;
import com.kaplich.calories.model.Product;
import com.kaplich.calories.repository.ClientRepository;
import com.kaplich.calories.repository.DishRepository;
import com.kaplich.calories.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@AllArgsConstructor
@Transactional
public class DishService {
    private final ProductRepository productRepository;
    private final DishRepository dishRepository;
    private final ClientRepository clientRepository;
    private CacheEntity dishCache;
    private CacheEntity clientCache;
    public List<DishDto> findAllDishes() {
        List<DishDto> dishDtoList = new ArrayList<>();
        for (Dish dish : dishRepository.findAll()) {
            DishDto dishDto = DishMapper.toDto(dish);
            dishDtoList.add(dishDto);
        }
        dishCache.put("all", dishDtoList);
        return dishDtoList;

    }


    public DishDto saveDish(final DishDto dishDto) {
        Dish dish = dishRepository.
                findByDishName(dishDto.getDishName());

        if (dish == null) {
            dish = DishMapper.toEntity(dishDto);
            dish.setProductList(new ArrayList<>());
            dishRepository.save(dish);
            dishCache.put("all", dishDto);
        }

        if (dishDto.getProductList() != null) {

            for (ProductDto productDto : dishDto.getProductList()) {
                Product product = productRepository.
                        findByProductName(productDto.getProductName());
                if (product == null) {
                    dish.getProductList().
                            add(ProductMapper.toEntity(productDto));

                } else {
                    dish.getProductList().add(product);
                }
                dishCache.put("all", dishDto);
            }
        }
        return dishDto;
    }

    public DishDto findByDishName(final String nameOfDish) {
        Object cachedObject = clientCache.get(nameOfDish);
        if (cachedObject instanceof DishDto dishDto) {
            return dishDto;
        }

        Dish dish = dishRepository.findByDishName(nameOfDish);
        return DishMapper.toDto(dish);


    }

    public DishDto findById(final Long id) {
        Dish dish = dishRepository.findDishById(id);
        return DishMapper.toDto(dish);


    }

    public void updateDish(final Long id, final DishDto updatedDish) {
        Dish dishToUpdate = dishRepository.findDishById(id);
        if(updatedDish.getCountOfCalories()!=0){
        dishToUpdate.setCountOfCalories(updatedDish.getCountOfCalories());
        }
        if(updatedDish.getClientDto()!=null) {
            dishToUpdate.setClient(ClientMapper.toEntity(updatedDish.getClientDto()));
        }
        dishRepository.save(dishToUpdate);
    }

    public void deleteDish(final String nameOfDish) {
        Dish dishToDelete = dishRepository.findByDishName(nameOfDish);
        if (dishToDelete != null) {
            dishRepository.delete(dishToDelete);
            dishCache.remove(dishToDelete.getDishName());
        }
    }

    public List<DishDto> findByClientNameAndCountOfCalories(
            @Param("clientName") final String clientName,
            @Param("countOfCalories") final double countOfCalories) {
        String cacheKey = clientName + "_" + countOfCalories;
        List<Dish> dishList = dishRepository.
                findByClientNameAndCountOfCalories(clientName,
                        countOfCalories);

        List<DishDto> dishDtoList = dishList.stream()
                .map(dish -> {
                    DishDto dishDto = new DishDto();
                    dishDto.setDishName(dish.getDishName());
                    dishDto.setCountOfCalories(dish.getCountOfCalories());
                    return dishDto;
                })
                .toList();
        dishCache.put(cacheKey, dishDtoList);
        return dishDtoList;
    }
    public List<ClientDto> findAllClients() {
        List<ClientDto> clientDtoList = new ArrayList<>();
        for (Client client : clientRepository.findAll()) {
            ClientDto clientDto = ClientMapper.toDto(client);
            clientDtoList.add(clientDto);
        }
        return clientDtoList;
    }
}
