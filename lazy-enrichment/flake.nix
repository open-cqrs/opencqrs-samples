{
  description = "OpenCQRS Lazy Enrichment Sample";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            pkgs.jdk21
            pkgs.gradle
            pkgs.docker
            pkgs.colima
          ];

          shellHook = ''
            export JAVA_HOME=${pkgs.jdk21}
          '';
        };
      });
}
