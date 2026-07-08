import DehumidifierRemote from "../Components/dehumidifier/DehumidifierRemote";

export default function DehumidifierPage() {
  return (
    <div className="container py-4">
      <h2 className="mb-4">Control dezumidificator</h2>

      <DehumidifierRemote />
    </div>
  );
}
