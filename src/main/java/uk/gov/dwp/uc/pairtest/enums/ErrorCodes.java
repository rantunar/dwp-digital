package uk.gov.dwp.uc.pairtest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCodes {
    ERROR01("Unknown application error"),
    ERROR02("Account Id is not valid"),
    ERROR03("Max ticket count purchase exceeded"),
    ERROR04("Adult ticket is not present"),
    ERROR05("Purchase data is null");
    private final String msg;
}
