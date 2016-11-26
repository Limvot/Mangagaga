with import <nixpkgs> {}; {
  freestyleEnv = stdenv.mkDerivation rec {
    name = "Mangagaga";
    #buildInputs = [ gradle_2_5 androidsdk androidsdk_extras ];
    buildInputs = [ gradle_2_5 androidenv.androidsdk_5_0_1 androidenv.androidsdk_5_0_1_extras ];
  };
}

