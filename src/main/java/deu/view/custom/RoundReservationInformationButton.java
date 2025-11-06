package deu.view.custom;

import deu.model.entity.RoomReservation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoundReservationInformationButton extends ButtonRound{

    RoomReservation roomReservation;
}