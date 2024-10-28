package com.capellax.ecommerce.api.controller.user;

import com.capellax.ecommerce.api.model.DataChange;
import com.capellax.ecommerce.model.Address;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.dao.AddressDAO;
import com.capellax.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AddressDAO addressDAO;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @GetMapping("/{userId}/address")
    public ResponseEntity<List<Address>> getAddress(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long userId
    ) {
        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressDAO.findByUser_Id(userId));
    }

    @PutMapping("{userId}/address")
    public ResponseEntity<Address> putAddress(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long userId,
            @RequestBody Address address
    ) {
        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        LocalUser refUser = new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        // return ResponseEntity.ok(addressDAO.save(address));
        Address savedAddress = addressDAO.save(address);
        simpMessagingTemplate.convertAndSend(
                "/topic/users/" + userId + "/address",
                new DataChange<>(DataChange.ChangeType.INSERT, address)
                );
        return ResponseEntity.ok(savedAddress);
    }

    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<?> patchAddress(
            @AuthenticationPrincipal LocalUser user,
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @RequestBody Address address
    ) {
        if (!userService.userHasPermissionToUser(user, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (address.getId() != null && address.getId().equals(addressId)) {
            if (address.getId() == addressId) {
                Optional<Address> opOriginalAddress = addressDAO.findById(addressId);
                if (opOriginalAddress.isPresent()) {
                    LocalUser originalUser = opOriginalAddress.get().getUser();
                    if (originalUser.getId() == userId) {
                        address.setUser(originalUser);
                        Address savedAddress = addressDAO.save(address);
                        simpMessagingTemplate.convertAndSend(
                                "/topic/users/" + userId + "/address",
                                new DataChange<>(DataChange.ChangeType.UPDATE, address)
                        );
                        return ResponseEntity.ok(savedAddress);
                    }
                }
            }
        } else {
            return ResponseEntity.badRequest().body("Address ID mismatch or null ID");
        }

        return ResponseEntity.badRequest().build();
    }

}
