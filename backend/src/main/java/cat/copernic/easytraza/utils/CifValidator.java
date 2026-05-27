package cat.copernic.easytraza.utils;

/**
 * Validador de NIF/CIF espanyol i NIE.
 */
public class CifValidator {

    private static final String NIF_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final String CIF_LETTER_DIGIT = "ABCDEFGHJUV";

    public static boolean isValid(String cif) {
        if (cif == null || cif.isEmpty()) return false;
        
        String clean = cif.replaceAll("[\\s\\-\\.,;_]", "").toUpperCase().trim();
        if (clean.length() < 8 || clean.length() > 10) return false;

        // Strict validation with control digit
        boolean strictValid = false;

        if (clean.matches("\\d{8}[A-Z]")) {
            strictValid = validarLetraNif(clean.substring(0, 8), clean.charAt(8));
        } else if (clean.matches("[XYZ]\\d{7}[A-Z]")) {
            String prefix = switch (clean.charAt(0)) {
                case 'X' -> "0"; case 'Y' -> "1"; case 'Z' -> "2"; default -> "";
            };
            strictValid = validarLetraNif(prefix + clean.substring(1, 8), clean.charAt(8));
        } else if (clean.matches("[ABCDEFGHJKLMNPQRSUVW]\\d{7}[0-9A-J]")) {
            strictValid = validarCif(clean.charAt(0), clean.substring(1, 8), clean.charAt(8));
        }

        if (strictValid) return true;

        // Lenient fallback: accept any Spanish tax ID format
        return clean.matches("\\d{8}[A-Z]") ||
               clean.matches("[XYZ]\\d{7}[A-Z]") ||
               clean.matches("[A-Z]\\d{7,9}") ||
               clean.matches("\\d{9}") ||
               clean.matches("[A-Z]{2}\\d{7}");
    }

    private static boolean validarLetraNif(String numero, char letra) {
        try {
            int resto = Integer.parseInt(numero) % 23;
            return NIF_LETTERS.charAt(resto) == letra;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean validarCif(char tipo, String digits, char control) {
        int suma = 0;
        for (int i = 0; i < 7; i++) {
            int d = Character.getNumericValue(digits.charAt(i));
            if (i % 2 == 0) {
                int doble = d * 2;
                suma += doble > 9 ? doble - 9 : doble;
            } else {
                suma += d;
            }
        }
        int esperado = (10 - (suma % 10)) % 10;
        if (CIF_LETTER_DIGIT.indexOf(tipo) >= 0) {
            return Character.isDigit(control) && Character.getNumericValue(control) == esperado;
        } else {
            return control == NIF_LETTERS.charAt(esperado);
        }
    }

    public static String getErrorMessage() {
        return "El CIF no és vàlid. Format: 8 dígits + lletra (NIF), X/Y/Z + 7 dígits + lletra (NIE), o lletra + 7 dígits + control (CIF)";
    }
}